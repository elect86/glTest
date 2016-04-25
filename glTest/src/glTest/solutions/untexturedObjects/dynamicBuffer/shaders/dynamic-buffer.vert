#version 450

// Attributes
#define POSITION    0
#define COLOR       1

// Uniform
#define TRANSFORM0  0
#define TRANSFORM1  1

// Interface
#define BLOCK   0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Input -----------------------------------------------------------------------
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec3 inColor;

// Uniforms / SSBO -------------------------------------------------------------
layout (location = TRANSFORM0) uniform mat4 viewProjection;
layout (binding = TRANSFORM1) uniform Transform
{
    mat4 world;
} t1;

// Output ----------------------------------------------------------------------
layout (location = BLOCK) out Block 
{
    vec3 color;
} outBlock;

// Functions -------------------------------------------------------------------
void main()
{
    vec3 worldPos = vec3(t1.world * vec4(inPos, 1));
    gl_Position = viewProjection * vec4(worldPos, 1);
    outBlock.color = inColor;
}
