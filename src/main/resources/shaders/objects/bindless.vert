#version 450

#include "semantic.glsl"


// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;
layout (location = TRANSFORM1) uniform mat4 world;

// Output ----------------------------------------------------------------------
layout (location = BLOCK) out Block
{
    vec3 color;
} outBlock;

// Functions -------------------------------------------------------------------
void main()
{
    vec3 worldPos = vec3(world * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}