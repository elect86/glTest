#version 330

// Attributes
#define POSITION    0

layout (std140, column_major) uniform;

layout (location = POSITION) in vec2 position;

uniform Transform0
{
    mat4 mat;
} t0;

void main()
{
    gl_Position = vec4(position, t0.mat[0].y, 1);
}
