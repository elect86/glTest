#version 440    // ARB_separate_shader_objects

// Interface
#define BLOCK   0

// Output
#define FRAG_COLOR  0


// Uniforms / SSBO -------------------------------------------------------------

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
