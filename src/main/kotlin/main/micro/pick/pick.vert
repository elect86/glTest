#version 330

layout (location = 0) in vec3 position;

/*out Block
{
    flat int id;
} outBlock;*/

void main() 
{
    gl_Position = vec4(-1, -1, 0, 1);
    if(gl_VertexID == 1)
        gl_Position = vec4(0, 1, 0, 1);
    if(gl_VertexID == 2)
        gl_Position = vec4(1, 0, 0, 1);
    //outBlock.id = 0;
}
