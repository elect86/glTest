#version 440    // ARB_separate_shader_objects

#include "semantic.glsl"


// Uniforms / SSBO ----------------------------------------------------------------------------------------------------
layout (location = DIFFUSE, bindless_sampler) uniform sampler2D tex;

// Input -----------------------------------------------------------------------
layout (location = BLOCK) in Block 
{
    vec3 color;
} inBlock;

//  Output ---------------------------------------------------------------------
layout(location = FRAG_COLOR) out vec4 outColor;

// Functions -------------------------------------------------------------------
void main()
{
    outColor = vec4(inBlock.color, 1);
}
