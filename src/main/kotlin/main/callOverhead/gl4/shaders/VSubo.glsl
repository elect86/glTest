#version 330



layout (location = 0) in vec3 Position;

layout(std140) uniform ubo {

    mat4 modelToClip;
};

void main()
{
    gl_Position = modelToClip * vec4(Position, 1.0);
}
