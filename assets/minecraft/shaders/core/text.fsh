#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#endif

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#endif

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#ifdef IS_GUI
    const float VANILLA_TEXT_COLOR = float(0x404040);
    const float NEW_TEXT_COLOR = float(0xaaaaaa);
#endif

void main() {
#ifdef IS_GRAYSCALE
    vec4 texColor = texture(Sampler0, texCoord0).rrrr;
#else
    vec4 texColor = texture(Sampler0, texCoord0);
#endif

#ifdef IS_SEE_THROUGH
    vec4 color = texColor * vertexColor;
#else
    vec4 color = texColor * vertexColor * ColorModulator;
#endif
    if (color.a < 0.1) {
        discard;
    }

#ifdef IS_GUI
    vec3 chn = floor(clamp(color.rgb, 0.0, 1.0) * 255.0 + vec3(0.5));
    float col = chn.x * (256.0 * 256.0) + chn.y * 256.0 + chn.z;
    if (col == VANILLA_TEXT_COLOR) {
        color.rgb = vec3(
            floor(NEW_TEXT_COLOR / (256.0 * 256.0)),
            floor(mod(NEW_TEXT_COLOR, 256.0 * 256.0) / 256.0),
            mod(NEW_TEXT_COLOR, 256.0)
        ) / 255.0;
    }
#endif

#ifdef IS_SEE_THROUGH
    fragColor = color * ColorModulator;
#elif defined(IS_GUI)
    fragColor = color;
#else
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#endif
}
