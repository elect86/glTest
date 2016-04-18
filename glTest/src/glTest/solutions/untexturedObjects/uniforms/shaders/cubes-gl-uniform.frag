#version 450

// Interface
#define BLOCK   0

// Output
#define FRAG_COLOR  0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

// Uniforms / SSBO ----------------------------------------------------------------------------------------------------

// Input --------------------------------------------------------------------------------------------------------------
layout (location = BLOCK) in Block 
{
    vec3 color;
} inBlock;

//  Output ------------------------------------------------------------------------------------------------------------
layout(location = FRAG_COLOR) out vec4 outColor;

// Functions ----------------------------------------------------------------------------------------------------------
void main()
{
    outColor = vec4(inBlock.color, 1);
}
