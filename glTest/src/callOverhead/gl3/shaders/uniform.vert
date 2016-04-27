#version 330

// Attributes
#define POSITION    0

layout (std140, column_major) uniform;

layout (location = POSITION) in vec2 position;

uniform float z;

void main()
{
    gl_Position = vec4(position, z, 1);
}
