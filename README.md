# glCalls

Micro benchmark in order to evaluate the *relative* weights between different GL state changes

I hit 105k framebuffer, 700k Program, 3.1M Texture Bindings, 3.4M Vertex Formats, 4.6M UBO Bindings, 6.3M Vertex Bindings and 15.2M Uniform Updates 

This can be also seen as the following.

On my machine a single framebuffer switch is equal to:

- 6.67 Program state changes
- 29 Texture Bindings state changes
- 32 Vertex Formats state changes
- 43 UBO Bindings state changes
- 60 Vertex Bindings state changes
- 144 Uniform Updates state changes

Specs:

- win7 x64
- gtx 770 (353.30)
- jogl 2.3.1


Jogl is 3-5% faster compared to Lwjgl
