package dev.quantumfusion.dashloader.data.image;

import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import net.minecraft.client.texture.MissingSprite;

@DashObject(MissingSprite.class)
public final class DashMissingSprite extends DashSpriteImpl implements DashSprite {
	public DashMissingSprite(DashSpriteAnimation animation, int image, boolean imageTransparent, int images, int x, int y, int width, int height, float uMin, float uMax, float vMin, float vMax) {
		super(animation, image, imageTransparent, images, x, y, width, height, uMin, uMax, vMin, vMax);
	}

	public DashMissingSprite(MissingSprite sprite, RegistryWriter writer) {
		super(sprite, writer);
	}
}
