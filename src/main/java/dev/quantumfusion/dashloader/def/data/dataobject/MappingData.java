package dev.quantumfusion.dashloader.def.data.dataobject;

import com.mojang.blaze3d.platform.TextureUtil;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.core.ui.DashLoaderProgress;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.feature.Feature;
import dev.quantumfusion.dashloader.def.data.VanillaData;
import dev.quantumfusion.dashloader.def.data.dataobject.mapping.*;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteAtlasTextureData;
import dev.quantumfusion.dashloader.def.mixin.accessor.AbstractTextureAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SpriteAtlasTextureAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.quantumfusion.dashloader.def.DashLoader.VANILLA_DATA;

@Data
@DataNullable
public class MappingData {
	public DashBlockStateData blockStateData;
	public DashFontManagerData fontManagerData;
	public DashModelData modelData;
	public DashParticleData predicateData;
	public DashSplashTextData splashTextData;
	public DashSpriteAtlasData spriteAtlasData;
	public DashShaderData shaderData;

	private transient List<Pair<Feature, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>>> atlasesToRegister;
	private transient final DashRegistryWriter registry;

	public MappingData(DashRegistryWriter registry) {
		this.registry = registry;
	}

	@SuppressWarnings("unused") // hyphen
	public MappingData(
			DashBlockStateData blockStateData,
			DashFontManagerData fontManagerData,
			DashModelData modelData,
			DashParticleData predicateData,
			DashSplashTextData splashTextData,
			DashSpriteAtlasData spriteAtlasData,
			DashShaderData shaderData) {
		this.blockStateData = blockStateData;
		this.fontManagerData = fontManagerData;
		this.modelData = modelData;
		this.predicateData = predicateData;
		this.splashTextData = splashTextData;
		this.spriteAtlasData = spriteAtlasData;
		this.shaderData = shaderData;
		this.registry = null;
	}


	public void map() {
		DashLoaderProgress.PROGRESS.setCurrentSubtask("Mapping", 5);

		if (Feature.MODEL_LOADER.active()) {
			blockStateData = new DashBlockStateData(VANILLA_DATA, registry);
			modelData = new DashModelData(VANILLA_DATA, registry);
			spriteAtlasData = new DashSpriteAtlasData(VANILLA_DATA, registry);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.PARTICLES.active()) {
			predicateData = new DashParticleData(VANILLA_DATA, registry);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.FONTS.active()) {
			fontManagerData = new DashFontManagerData(VANILLA_DATA, registry);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.SPLASH_TEXT.active()) {
			splashTextData = new DashSplashTextData(VANILLA_DATA);
		} DashLoaderProgress.PROGRESS.completedSubTask();

		if (Feature.SHADERS.active()) {
			shaderData = new DashShaderData(VANILLA_DATA);
		} DashLoaderProgress.PROGRESS.completedSubTask();
	}

	public void export(DashRegistryReader registry, VanillaData vanillaData) {
		var spriteData = DashUtil.nullable(this.spriteAtlasData, registry, DashSpriteAtlasData::export);
		var particleData = DashUtil.nullable(this.predicateData, registry, DashParticleData::export);
		vanillaData.loadCacheData(
				DashUtil.nullable(spriteData, Pair::getLeft),
				DashUtil.nullable(blockStateData, registry, DashBlockStateData::export),
				DashUtil.nullable(modelData, registry, DashModelData::export),
				DashUtil.nullable(particleData, Pair::getLeft),
				DashUtil.nullable(fontManagerData, registry, DashFontManagerData::export),
				DashUtil.nullable(splashTextData, DashSplashTextData::export),
				DashUtil.nullable(shaderData, DashShaderData::export)
		);
		atlasesToRegister = new ArrayList<>();

		if (spriteData != null) {
			spriteData.getValue().forEach(atlasTexture -> atlasesToRegister.add(Pair.of(Feature.MODEL_LOADER, Pair.of(atlasTexture, vanillaData.getAtlasData(atlasTexture)))));
		}

		if (particleData != null) {
			SpriteAtlasTexture texture = particleData.getRight();
			atlasesToRegister.add(Pair.of(Feature.PARTICLES, Pair.of(texture, vanillaData.getAtlasData(texture))));
		}

		modelData = null;
		spriteAtlasData = null;
		blockStateData = null;
		fontManagerData = null;
		splashTextData = null;
	}

	public void registerAtlases(TextureManager textureManager, Feature feature) {
		atlasesToRegister.forEach((pair) -> {
			if (pair.getLeft() == feature) {
				final Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData> atlas = pair.getRight();
				registerAtlas(atlas.getLeft(), atlas.getRight(), textureManager);
			}
		});
	}

	@Nullable
	public SpriteAtlasTexture getAtlas(Identifier identifier) {
		for (Pair<Feature, Pair<SpriteAtlasTexture, DashSpriteAtlasTextureData>> pair : atlasesToRegister) {
			final SpriteAtlasTexture atlas = pair.getRight().getLeft();
			if (identifier.equals(atlas.getId())) {
				return atlas;
			}
		}
		return null;
	}

	public void registerAtlas(SpriteAtlasTexture atlasTexture, DashSpriteAtlasTextureData data, TextureManager textureManager) {
		//atlas registration
		final Identifier id = atlasTexture.getId();
		final int glId = TextureUtil.generateTextureId();
		final int width = data.width();
		final int maxLevel = data.maxLevel();
		final int height = data.height();
		((AbstractTextureAccessor) atlasTexture).setGlId(glId);
		//ding dong lwjgl here are their styles

		TextureUtil.prepareImage(glId, maxLevel, width, height);
		((SpriteAtlasTextureAccessor) atlasTexture).getSprites().forEach((identifier, sprite) -> {
			final SpriteAccessor access = (SpriteAccessor) sprite;
			access.setAtlas(atlasTexture);
			access.setId(identifier);
			sprite.upload();
		});

		//helu textures here are the atlases
		textureManager.registerTexture(id, atlasTexture);
		atlasTexture.setFilter(false, maxLevel > 0);
		DashLoader.LOGGER.info("Allocated: {}x{}x{} {}-atlas", width, height, maxLevel, id);
	}
}
