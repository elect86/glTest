#version 330

// Outgoing final color.
layout (location = 0) out int outputColor;
//layout (location = 0) out vec4 outputColor;

/*in Block
{
    flat int id;
} inBlock;*/

void main() 
{    
    //outputColor = inBlock.id;
    outputColor = 9;
    //outputColor = vec4(0, 1, 0, 1);
}