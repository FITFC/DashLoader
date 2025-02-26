package dev.quantumfusion.dashloader.mixin.accessor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlUniform.class)
public interface GlUniformAccessor {

	@Accessor
	@Mutable
	void setCount(int count);

	@Accessor
	@Mutable
	void setDataType(int dataType);

	@Accessor
	@Mutable
	void setIntData(IntBuffer intData);

	@Accessor
	@Mutable
	void setFloatData(FloatBuffer floatData);

	@Accessor
	@Mutable
	void setName(String name);

	@Accessor
	@Mutable
	void setProgram(GlShader program);
}
