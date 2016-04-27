# glTest

jogl port of the [apitest](https://github.com/nvMcJohn/apitest).

## Changing Problems / Solutions

The active problem is changed by pressing the left or right arrow keys.
The active solution is changed by pressing the up or down arrow keys.

first line is C, second is Java

Null Problem:

* Null Solution -> 15k fps

Dynamic Streaming:

* GLBufferSubData -> 2.1 fps

		CPU time: 444 ms, GPU time: 455 ms, theor. FPS: 2,194
	
		CPU time: 256 ms, GPU time: 458 ms, theor. FPS: 2,18
* GLMapUnsynchronized -> 9.0 fps

		CPU time: 115 ms, GPU time: 114 ms, theor. FPS: 8.7
	
		CPU time: 208 ms, GPU time: 206 ms, theor. FPS: 4,8
* GLMapPersistent -> 117 fps

		CPU time: 8 ms, GPU time: 6 ms, theor. FPS: 165
	
		CPU time: 16 ms, GPU time: 10,9 ms, theor. FPS: 91,3
		
	Direct3D11:
	
		D3D11UpdateSubresource -> CPU time: 407 ms, FPS: 2,06
		
		D3D11MapNoOverwrite -> CPU time: 14 ms, FPS: 66

Untextured Objects:

* GLUniform -> 26 fps

		CPU time: 35 ms, GPU time: 30 ms, theor. FPS: 33
	
		CPU time: 47 ms, GPU time: 44 ms, theor. FPS: 22.3
* GLDrawLoop -> 43 fps
	
		CPU time: 23 ms, GPU time: 17 ms, theor. FPS: 58
	
		CPU time: 22 ms, GPU time: 20,7 ms, theor. FPS: 48
* GLMultiDraw-SDP -> 66 fps

* GLMultiDraw-NoSDP -> 109 fps

* GLMultiDrawBuffer-SDP -> 63 fps
	
		CPU time: 21 ms, GPU time: 16 ms, theor. FPS: 61
	
		CPU time: 5.8 ms, GPU time: 15.9 ms, theor. FPS: 62.5
* GLMultiDrawBuffer-NoSDP -> 115 fps
	
		CPU time: 13 ms, GPU time: 7.6 ms, theor. FPS: 130
	
		CPU time: 6 ms, GPU time: 7.9 ms, theor. FPS: 126.5
* GLBindless -> 25 fps
	
		CPU time: 37 ms, GPU time: 32 ms, theor. FPS: 31
	
		CPU time: 162 ms, GPU time: 158 ms, theor. FPS: 6,3
* GLBindlessIndirect -> 33 fps
	
		CPU time: 40 ms, GPU time: 28.8 ms, theor. FPS: 34.5
	
		CPU time: 22 ms, GPU time: 31 ms, theor. FPS: 31,5
* GLBufferRange -> 15 fps
	
		CPU time: 60 ms, GPU time: 55 ms, theor. FPS: 18
	
		CPU time: 78,6 ms, GPU time: 76,5 ms, theor. FPS: 13,1
* GLBufferStorage-SDP -> 73 fps
	
		CPU time: 19 ms, GPU time: 13 ms, theor. FPS: 75
	
		CPU time: 5 ms, GPU time: 14,6 ms, theor. FPS: 68,4
* GLBufferStorage-NoSDP -> 137 fps
	
		CPU time: 11 ms, GPU time: 4.7 ms, theor. FPS: 211
	
		CPU time: 5,1 ms, GPU time: 6 ms, theor. FPS: 164,3
* GLDynamicBuffer -> 23 fps
	
		CPU time: 38 ms, GPU time: 33.6 ms, theor. FPS: 29.7
	
		CPU time: 75 ms, GPU time: 71,9 ms, theor. FPS: 13,9
* GLMapUnsynchronized -> 1.2 fps
	
		CPU time: 790 ms, GPU time: 790 ms, theor. FPS: 1.2
	
		CPU time: 747 ms, GPU time: 783 ms, theor. FPS: 1,28
* GLMapPersistent -> 48 fps
	
		CPU time: 20 ms, GPU time: 16 ms, theor. FPS: 62
	
		CPU time: 25,2 ms, GPU time: 22,6 ms, theor. FPS: 44,2
* GLTexCoord -> 48 fps
	
		CPU time: 27 ms, GPU time: 23 ms, theor. FPS: 43
	
		CPU time: 36,9 ms, GPU time: 34,8 ms, theor. FPS: 28,7

	Direct3D11:
	
		D3D11Naive -> CPU time: 20 ms, FPS: 49,5
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
