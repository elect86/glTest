#version 330

// Attributes
#define POSITION    0

layout (location = POSITION) in vec2 position;

void main()
{
    gl_Position = vec4(position, 0, 1);
}
