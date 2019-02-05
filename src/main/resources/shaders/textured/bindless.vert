#version 440
#extension GL_ARB_shader_storage_buffer_object : require

#include "semantic.glsl"


// Uniforms / SSBO ----------------------------------------------------------------------------------------------------
layout (binding = CONSTANT) buffer CB0
{
    mat4 transforms[];
};

uniform mat4 viewProjection;
uniform int drawID;

// Input --------------------------------------------------------------------------------------------------------------
layout(location = POSITION) in vec3 inPos;
layout(location = TEX_COORD) in vec2 inTexCoord;

// Output -------------------------------------------------------------------------------------------------------------
layout(location = BLOCK) out Block {
    vec2 uv;
} outBlock;

// Functions ----------------------------------------------------------------------------------------------------------
void main()
{
    mat4 world = transforms[drawID];
    vec4 worldPos = world * vec4(inPos, 1);
    gl_Position = viewProjection * worldPos;

    outBlock.uv = inTexCoord;
}