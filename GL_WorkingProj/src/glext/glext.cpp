#include <windows.h>
#include "glext.h"


PFNGLGETPROGRAMIVARBPROC glGetProgramivARB;

//min max
PFNGLMINMAXPROC glMinmax;
PFNGLGETMINMAXPROC glGetMinmax;

// stencil
PFNGLACTIVESTENCILFACEEXTPROC glActiveStencilFaceEXT;

//blend
PFNGLBLENDEQUATIONPROC glBlendEquation;
PFNGLBLENDFUNCSEPARATEEXTPROC glBlendFuncSeparateEXT;

//clear color
PFNGLCLEARCOLORIIEXTPROC glClearColorIiEXT;
PFNGLCLEARCOLORIUIEXTPROC glClearColorIuiEXT;

PFNGLCLEARBUFFERIVPROC glClearBufferiv;
PFNGLCLEARBUFFERUIVPROC glClearBufferuiv;
PFNGLCLEARBUFFERFVPROC glClearBufferfv;
PFNGLCLEARBUFFERFIPROC glClearBufferfi;

// textures
PFNGLACTIVETEXTUREARBPROC glActiveTextureARB;
PFNGLTEXIMAGE3DPROC glTexImage3D;
PFNGLCOPYTEXSUBIMAGE3DEXTPROC glCopyTexSubImage3DEXT;
PFNGLCOPYTEXSUBIMAGE3DPROC glCopyTexSubImage3D;
PFNGLMULTITEXCOORD3FARBPROC	glMultiTexCoord3fARB;
PFNGLMULTITEXCOORD4FARBPROC	glMultiTexCoord4fARB;
PFNGLMULTITEXCOORD4FVARBPROC	glMultiTexCoord4fvARB;
PFNGLMULTITEXCOORD2FARBPROC glMultiTexCoord2fARB;
PFNGLMULTITEXCOORD2FVARBPROC glMultiTexCoord2fvARB;
PFNGLCLIENTACTIVETEXTUREARBPROC glClientActiveTextureARB;
PFNGLTEXSUBIMAGE3DPROC glTexSubImage3D;
PFNGLCOMPRESSEDTEXIMAGE2DPROC glCompressedTexImage2D;
PFNGLCOMPRESSEDTEXIMAGE2DARBPROC glCompressedTexImage2DARB;
PFNGLTEXPARAMETERIIVPROC glTexParameterIiv;
PFNGLGENERATEMIPMAPPROC glGenerateMipmap;

PFNGLMEMORYBARRIERPROC glMemoryBarrier;
PFNGLTEXTUREBARRIERPROC glTextureBarrier;

PFNGLTEXSTORAGE3DPROC glTexStorage3D;
PFNGLTEXSTORAGE2DPROC glTexStorage2D;

// arb programs
PFNGLGENPROGRAMSARBPROC glGenProgramsARB;
PFNGLBINDPROGRAMARBPROC glBindProgramARB;
PFNGLDELETEPROGRAMSARBPROC glDeleteProgramsARB;
PFNGLPROGRAMSTRINGARBPROC glProgramStringARB;
PFNGLPROGRAMENVPARAMETER4FARBPROC glProgramEnvParameter4fARB;
PFNGLPROGRAMENVPARAMETER4FVARBPROC glProgramEnvParameter4fvARB;
PFNGLPROGRAMLOCALPARAMETER4FARBPROC glProgramLocalParameter4fARB;
PFNGLPROGRAMLOCALPARAMETER4FVARBPROC glProgramLocalParameter4fvARB;

//shaders
PFNGLATTACHSHADERPROC glAttachShader;
PFNGLBINDATTRIBLOCATIONPROC glBindAttribLocation;
PFNGLCOMPILESHADERPROC glCompileShader;
PFNGLCREATEPROGRAMPROC glCreateProgram;
PFNGLCREATESHADERPROC glCreateShader;
PFNGLDELETEPROGRAMPROC glDeleteProgram;
PFNGLDELETESHADERPROC glDeleteShader;
PFNGLDETACHSHADERPROC glDetachShader;
PFNGLSHADERSOURCEPROC glShaderSource;
PFNGLLINKPROGRAMPROC glLinkProgram;
PFNGLUSEPROGRAMPROC glUseProgram;
PFNGLPROGRAMUNIFORM1IPROC glProgramUniform1i;
PFNGLVALIDATEPROGRAMPROC glValidateProgram;
PFNGLGETACTIVEATTRIBPROC glGetActiveAttrib;
PFNGLGETACTIVEUNIFORMPROC glGetActiveUniform;


//uniform buffer
PFNGLUNIFORMBUFFEREXTPROC glUniformBufferEXT;
//PFNGLGETUNIFORMLOCATIONARBPROC glGetUniformLocationARB;
PFNGLGETUNIFORMBLOCKINDEXPROC glGetUniformBlockIndex;
PFNGLUNIFORMBLOCKBINDINGPROC glUniformBlockBinding;

//texture buffer
PFNGLTEXBUFFERARBPROC glTexBufferARB;

//glPrimitiveRestartIndex
PFNGLPRIMITIVERESTARTINDEXPROC glPrimitiveRestartIndex;
PFNGLPRIMITIVERESTARTINDEXNVPROC glPrimitiveRestartIndexNV;

//instancing
PFNGLDRAWELEMENTSINSTANCEDEXTPROC glDrawElementsInstancedEXT;
PFNGLDRAWARRAYSINSTANCEDPROC glDrawArraysInstanced;
PFNGLDRAWELEMENTSINSTANCEDPROC glDrawElementsInstanced;
PFNGLDRAWELEMENTSINSTANCEDBASEINSTANCEPROC glDrawElementsInstancedBaseInstance;
PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXBASEINSTANCEPROC glDrawElementsInstancedBaseVertexBaseInstance;
PFNGLMULTIDRAWELEMENTSINDIRECTPROC glMultiDrawElementsIndirect;
PFNGLDRAWARRAYSINDIRECTPROC glDrawArraysIndirect;

//draw
PFNGLDRAWELEMENTSBASEVERTEXPROC glDrawElementsBaseVertex;
PFNGLDRAWRANGEELEMENTSBASEVERTEXPROC glDrawRangeElementsBaseVertex;
PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXPROC glDrawElementsInstancedBaseVertex;
PFNGLMULTIDRAWELEMENTSBASEVERTEXPROC glMultiDrawElementsBaseVertex;


PFNGLTEXBUFFERPROC glTexBuffer;
//PFNGLPRIMITIVERESTARTINDEXPROC glPrimitiveRestartIndex;
PFNGLCOPYBUFFERSUBDATAPROC glCopyBufferSubData;


// nv programs
PFNGLGENPROGRAMSNVPROC glGenProgramsNV;
PFNGLBINDPROGRAMNVPROC glBindProgramNV;
PFNGLDELETEPROGRAMSNVPROC glDeleteProgramsNV;
PFNGLLOADPROGRAMNVPROC glLoadProgramNV;
PFNGLPROGRAMNAMEDPARAMETER4FNVPROC glProgramNamedParameter4fNV;
PFNGLPROGRAMNAMEDPARAMETER4FVNVPROC glProgramNamedParameter4fvNV;

// attrib arrays
PFNGLENABLEVERTEXATTRIBARRAYARBPROC glEnableVertexAttribArrayARB;
PFNGLVERTEXATTRIBPOINTERARBPROC glVertexAttribPointerARB;
PFNGLDISABLEVERTEXATTRIBARRAYARBPROC glDisableVertexAttribArrayARB;
PFNGLVERTEXATTRIB4FVARBPROC glVertexAttrib4fvARB;
PFNGLGETATTRIBLOCATIONARBPROC glGetAttribLocation;

PFNGLVERTEXATTRIBPOINTERPROC glVertexAttribPointer;
PFNGLENABLEVERTEXATTRIBARRAYPROC glEnableVertexAttribArray;
PFNGLDISABLEVERTEXATTRIBARRAYPROC glDisableVertexAttribArray;
PFNGLVERTEXATTRIBDIVISORPROC glVertexAttribDivisor;


// vertex buffer object
PFNGLGENBUFFERSARBPROC glGenBuffersARB;
PFNGLBINDBUFFERARBPROC glBindBufferARB;
PFNGLBUFFERDATAARBPROC glBufferDataARB;
PFNGLDELETEBUFFERSARBPROC glDeleteBuffersARB;

PFNGLDRAWRANGEELEMENTSPROC glDrawRangeElements;
PFNGLMAPBUFFERARBPROC glMapBufferARB;
PFNGLUNMAPBUFFERARBPROC glUnmapBufferARB;

PFNGLBINDBUFFERRANGEPROC glBindBufferRange;
PFNGLBINDBUFFERBASEPROC glBindBufferBase;
PFNGLBINDIMAGETEXTUREPROC glBindImageTexture;


PFNGLBINDBUFFERPROC glBindBuffer;
PFNGLDELETEBUFFERSPROC glDeleteBuffers;
PFNGLGENBUFFERSPROC glGenBuffers;
PFNGLBUFFERDATAPROC glBufferData;
PFNGLMAPBUFFERPROC glMapBuffer;
PFNGLUNMAPBUFFERPROC glUnmapBuffer;
PFNGLMAPBUFFERRANGEPROC glMapBufferRange;

// vertex array
PFNGLBINDVERTEXARRAYPROC glBindVertexArray;
PFNGLDELETEVERTEXARRAYSPROC glDeleteVertexArray;
PFNGLGENVERTEXARRAYSPROC glGenVertexArrays;
//PFNGLISVERTEXARRAYPROC glIsVertexArray;

//transform feedback
PFNGLBINDTRANSFORMFEEDBACKPROC glBindTransformFeedback;
PFNGLDELETETRANSFORMFEEDBACKSPROC glDeleteTransformFeedback;
PFNGLGENTRANSFORMFEEDBACKSPROC glGenTransformFeedback;
PFNGLBEGINTRANSFORMFEEDBACKPROC glBeginTransformFeedback;
PFNGLENDTRANSFORMFEEDBACKPROC glEndTransformFeedback;
PFNGLTRANSFORMFEEDBACKVARYINGSPROC glTransformFeedbackVaryings;
PFNGLTRANSFORMFEEDBACKATTRIBSNVPROC glTransformFeedbackAttribsNV;

PFNGLBINDBUFFERBASENVPROC glBindBufferBaseNV;
PFNGLBEGINTRANSFORMFEEDBACKNVPROC glBeginTransformFeedbackNV;
PFNGLENDTRANSFORMFEEDBACKNVPROC glEndTransformFeedbackNV;
PFNGLBINDBUFFEROFFSETNVPROC glBindBufferOffsetNV;
PFNGLACTIVEVARYINGNVPROC glActiveVaryingNV;
PFNGLGETVARYINGLOCATIONNVPROC glGetVaryingLocationNV;
PFNGLTRANSFORMFEEDBACKVARYINGSNVPROC glTransformFeedbackVaryingsNV;



// occlision query
PFNGLGENQUERIESARBPROC glGenQueriesARB;
PFNGLDELETEQUERIESARBPROC glDeleteQueriesARB;
PFNGLBEGINQUERYARBPROC glBeginQueryARB;
PFNGLENDQUERYARBPROC glEndQueryARB;
PFNGLGETQUERYIVARBPROC glGetQueryivARB;
PFNGLGETQUERYOBJECTIVARBPROC glGetQueryObjectivARB;
PFNGLGETQUERYOBJECTUIVARBPROC glGetQueryObjectuivARB;
PFNGLGETQUERYOBJECTUI64VPROC glGetQueryObjectui64v;
PFNGLQUERYCOUNTERPROC glQueryCounter;
PFNGLBEGINQUERYINDEXEDPROC glBeginQueryIndexed;
PFNGLENDQUERYINDEXEDPROC glEndQueryIndexed;
PFNGLGETQUERYINDEXEDIVPROC glGetQueryIndexediv;

PFNGLGENQUERIESPROC glGenQueries;
PFNGLDELETEQUERIESPROC glDeleteQueries;
PFNGLBEGINQUERYPROC glBeginQuery;
PFNGLENDQUERYPROC glEndQuery;
PFNGLGETQUERYOBJECTIVPROC glGetQueryObjectiv;


// glsl
PFNGLCREATEPROGRAMOBJECTARBPROC glCreateProgramObjectARB;
PFNGLCREATESHADEROBJECTARBPROC glCreateShaderObjectARB;
PFNGLSHADERSOURCEARBPROC glShaderSourceARB;
PFNGLCOMPILESHADERARBPROC glCompileShaderARB;
PFNGLATTACHOBJECTARBPROC glAttachObjectARB;
PFNGLDETACHOBJECTARBPROC glDetachObjectARB;
PFNGLDELETEOBJECTARBPROC glDeleteObjectARB;
PFNGLBINDATTRIBLOCATIONARBPROC glBindAttribLocationARB;
PFNGLLINKPROGRAMARBPROC glLinkProgramARB;
PFNGLGETINFOLOGARBPROC glGetInfoLogARB;
PFNGLGETOBJECTPARAMETERIVARBPROC glGetObjectParameterivARB;
PFNGLVALIDATEPROGRAMARBPROC glValidateProgramARB;
PFNGLUSEPROGRAMOBJECTARBPROC glUseProgramObjectARB;
PFNGLGETUNIFORMLOCATIONARBPROC glGetUniformLocationARB;
PFNGLUNIFORM1FARBPROC glUniform1fARB;
PFNGLUNIFORM1FVARBPROC glUniform1fvARB;
PFNGLUNIFORM2FARBPROC glUniform2fARB;
PFNGLUNIFORM2FVARBPROC glUniform2fvARB;
PFNGLUNIFORM3FARBPROC glUniform3fARB;
PFNGLUNIFORM3FVARBPROC glUniform3fvARB;
PFNGLUNIFORM4FARBPROC glUniform4fARB;
PFNGLUNIFORM4FVARBPROC glUniform4fvARB;
PFNGLUNIFORMMATRIX3FVARBPROC glUniformMatrix3fvARB;
PFNGLUNIFORMMATRIX4FVARBPROC glUniformMatrix4fvARB;
PFNGLUNIFORM3IVARBPROC glUniform3ivARB;
PFNGLUNIFORM4IVARBPROC glUniform4ivARB;

PFNGLGETSHADERINFOLOGPROC glGetShaderInfoLog;
PFNGLGETPROGRAMINFOLOGPROC glGetProgramInfoLog;
PFNGLGETSHADERIVPROC glGetShaderiv;
PFNGLGETPROGRAMIVPROC glGetProgramiv;

PFNGLGETUNIFORMLOCATIONPROC glGetUniformLocation;
PFNGLUNIFORM1IPROC glUniform1i;

PFNGLUNIFORM1FPROC glUniform1f;
PFNGLUNIFORM2FPROC glUniform2f;
PFNGLUNIFORM3FPROC glUniform3f;
PFNGLUNIFORM4FPROC glUniform4f;
PFNGLUNIFORM1FVPROC glUniform1fv;
PFNGLUNIFORM2FVPROC glUniform2fv;
PFNGLUNIFORM3FVPROC glUniform3fv;
PFNGLUNIFORM4FVPROC glUniform4fv;
PFNGLUNIFORM4IVPROC glUniform4iv;
PFNGLUNIFORM2IPROC glUniform2i;

PFNGLUNIFORMMATRIX2FVPROC glUniformMatrix2fv;
PFNGLUNIFORMMATRIX3FVPROC glUniformMatrix3fv;
PFNGLUNIFORMMATRIX4FVPROC glUniformMatrix4fv;

PFNGLACTIVETEXTUREPROC glActiveTexture;


//texture sampler
PFNGLGENSAMPLERSPROC glGenSamplers;
PFNGLDELETESAMPLERSPROC glDeleteSamplers;
PFNGLBINDSAMPLERPROC glBindSampler;
PFNGLSAMPLERPARAMETERIPROC glSamplerParameteri;
PFNGLSAMPLERPARAMETERFVPROC glSamplerParameterfv;


// EXT_depth_bounds_test
PFNGLDEPTHBOUNDSEXTPROC			glDepthBoundsEXT;

// EXT_framebuffer_object
PFNGLISRENDERBUFFEREXTPROC						glIsRenderbufferEXT;
PFNGLBINDRENDERBUFFEREXTPROC					glBindRenderbufferEXT;
PFNGLDELETERENDERBUFFERSEXTPROC					glDeleteRenderbuffersEXT;
PFNGLGENRENDERBUFFERSEXTPROC					glGenRenderbuffersEXT;
PFNGLRENDERBUFFERSTORAGEEXTPROC					glRenderbufferStorageEXT;
PFNGLGETRENDERBUFFERPARAMETERIVEXTPROC			glGetRenderbufferParameterivEXT;
PFNGLISFRAMEBUFFEREXTPROC						glIsFramebufferEXT;
PFNGLDELETEFRAMEBUFFERSEXTPROC					glDeleteFramebuffersEXT;
PFNGLFRAMEBUFFERTEXTURE1DEXTPROC				glFramebufferTexture1DEXT;
PFNGLFRAMEBUFFERTEXTURE3DEXTPROC				glFramebufferTexture3DEXT;
PFNGLFRAMEBUFFERRENDERBUFFEREXTPROC				glFramebufferRenderbufferEXT;
PFNGLGETFRAMEBUFFERATTACHMENTPARAMETERIVEXTPROC	glGetFramebufferAttachmentParameterivEXT;
PFNGLGENERATEMIPMAPEXTPROC						glGenerateMipmapEXT;
PFNGLFRAMEBUFFERTEXTURELAYEREXTPROC				glFramebufferTextureLayerEXT;

PFNGLGENFRAMEBUFFERSPROC glGenFramebuffers;
PFNGLBINDFRAMEBUFFERPROC glBindFramebuffer;
PFNGLFRAMEBUFFERTEXTURE2DPROC glFramebufferTexture2D;
PFNGLCHECKFRAMEBUFFERSTATUSPROC glCheckFramebufferStatus;
PFNGLDELETEFRAMEBUFFERSPROC glDeleteFramebuffers;

PFNGLBINDFRAGDATALOCATIONPROC glBindFragDataLocation;

// GL_EXT_texture3D
PFNGLTEXIMAGE3DEXTPROC                  glTexImage3DEXT;

// draw buffers from OpenGL 2.0
PFNGLDRAWBUFFERSPROC							glDrawBuffers           = NULL;

// ATI_draw_buffers
PFNGLDRAWBUFFERSATIPROC							glDrawBuffersATI        = NULL;

//glProvokingVertex
PFNGLPROVOKINGVERTEXPROC glProvokingVertex;

//debug mode
PFNGLDEBUGMESSAGECONTROLARBPROC   glDebugMessageControlARB;
PFNGLDEBUGMESSAGEINSERTARBPROC    glDebugMessageInsertARB;
PFNGLDEBUGMESSAGECALLBACKARBPROC  glDebugMessageCallbackARB;
PFNGLGETDEBUGMESSAGELOGARBPROC    glGetDebugMessageLogARB;

//wgl
PFNWGLCHOOSEPIXELFORMATARBPROC wglChoosePixelFormatARB = NULL;
PFNWGLCREATEPBUFFERARBPROC wglCreatePbufferARB = NULL;
PFNWGLGETPBUFFERDCARBPROC wglGetPbufferDCARB = NULL;
PFNWGLRELEASEPBUFFERDCARBPROC wglReleasePbufferDCARB = NULL;
PFNWGLDESTROYPBUFFERARBPROC wglDestroyPbufferARB = NULL;
PFNWGLQUERYPBUFFERARBPROC wglQueryPbufferARB = NULL;
PFNWGLBINDTEXIMAGEARBPROC wglBindTexImageARB = NULL;
PFNWGLRELEASETEXIMAGEARBPROC wglReleaseTexImageARB = NULL;
PFNWGLGETPIXELFORMATATTRIBIVARBPROC wglGetPixelFormatAttribivARB = NULL;
PFNWGLSETPBUFFERATTRIBARBPROC wglSetPbufferAttribARB = NULL;
PFNWGLGETEXTENSIONSSTRINGARBPROC wglGetExtensionsStringARB = NULL;


//----------------------------------------------------------------------------------------------------------------

bool    isExtensionSupported ( const char * ext, const char * extList )
{
    const char * start = extList;
    const char * ptr;

    while ( ( ptr = strstr ( start, ext ) ) != NULL )
    {
		// we've found, ensure name is exactly ext
        const char * end = ptr + strlen ( ext );

        if ( isspace ( *end ) || *end == '\0' )
            return true;

        start = end;
    }

    return false;
}

bool    isExtensionSupported ( const char * ext )
{
    const char * extensions = (const char *) glGetString ( GL_EXTENSIONS );

    if ( isExtensionSupported ( ext, extensions ) )
	    return true;

	if ( wglGetExtensionsStringARB != NULL )
	{
		const char * wgl_extensions = (const char *) wglGetExtensionsStringARB ( wglGetCurrentDC () );
	    if ( isExtensionSupported ( ext, wgl_extensions ) )
		    return true;
	}

	return false;
}

//----------------------------------------------------------------------------------------------------------------

void glext_init() {

#ifdef _WIN32
#define GET_PROC_ADDRESS(a,b) b = (a)wglGetProcAddress(#b)
#else
#define GET_PROC_ADDRESS(a,b) b = (a)glXGetProcAddressARB((const GLubyte*)#b)
#endif
	

 	GET_PROC_ADDRESS(PFNGLGETPROGRAMIVARBPROC,glGetProgramivARB);

// min max
	GET_PROC_ADDRESS(PFNGLMINMAXPROC,glMinmax);
	GET_PROC_ADDRESS(PFNGLGETMINMAXPROC,glGetMinmax);


// stencil
	GET_PROC_ADDRESS(PFNGLACTIVESTENCILFACEEXTPROC,glActiveStencilFaceEXT);

// blend
	GET_PROC_ADDRESS(PFNGLBLENDEQUATIONPROC,glBlendEquation);
	GET_PROC_ADDRESS(PFNGLBLENDFUNCSEPARATEEXTPROC,glBlendFuncSeparateEXT);

//clear color
	GET_PROC_ADDRESS(PFNGLCLEARCOLORIIEXTPROC,glClearColorIiEXT);
	GET_PROC_ADDRESS(PFNGLCLEARCOLORIUIEXTPROC,glClearColorIuiEXT);
	
	GET_PROC_ADDRESS(PFNGLCLEARBUFFERIVPROC, glClearBufferiv);
	GET_PROC_ADDRESS(PFNGLCLEARBUFFERUIVPROC, glClearBufferuiv);
	GET_PROC_ADDRESS(PFNGLCLEARBUFFERFVPROC, glClearBufferfv);
	GET_PROC_ADDRESS(PFNGLCLEARBUFFERFIPROC, glClearBufferfi);

// textures
	GET_PROC_ADDRESS(PFNGLACTIVETEXTUREARBPROC, glActiveTextureARB);
	GET_PROC_ADDRESS(PFNGLTEXIMAGE3DPROC,glTexImage3D);
	GET_PROC_ADDRESS(PFNGLCOPYTEXSUBIMAGE3DEXTPROC,glCopyTexSubImage3DEXT);
	GET_PROC_ADDRESS(PFNGLCOPYTEXSUBIMAGE3DPROC,glCopyTexSubImage3D);
	GET_PROC_ADDRESS(PFNGLMULTITEXCOORD3FARBPROC,glMultiTexCoord3fARB);
	GET_PROC_ADDRESS(PFNGLMULTITEXCOORD4FARBPROC,glMultiTexCoord4fARB);
	GET_PROC_ADDRESS(PFNGLMULTITEXCOORD4FVARBPROC,glMultiTexCoord4fvARB);
	GET_PROC_ADDRESS(PFNGLMULTITEXCOORD2FARBPROC,glMultiTexCoord2fARB);
	GET_PROC_ADDRESS(PFNGLMULTITEXCOORD2FVARBPROC,glMultiTexCoord2fvARB);
	GET_PROC_ADDRESS(PFNGLCLIENTACTIVETEXTUREARBPROC,glClientActiveTextureARB);
	GET_PROC_ADDRESS(PFNGLTEXSUBIMAGE3DPROC,glTexSubImage3D);
	GET_PROC_ADDRESS(PFNGLCOMPRESSEDTEXIMAGE2DPROC,glCompressedTexImage2D);
	GET_PROC_ADDRESS(PFNGLCOMPRESSEDTEXIMAGE2DARBPROC,glCompressedTexImage2DARB);
	GET_PROC_ADDRESS(PFNGLTEXPARAMETERIIVPROC, glTexParameterIiv);
	GET_PROC_ADDRESS(PFNGLGENERATEMIPMAPPROC, glGenerateMipmap);

	GET_PROC_ADDRESS(PFNGLMEMORYBARRIERPROC, glMemoryBarrier);
	GET_PROC_ADDRESS(PFNGLTEXTUREBARRIERPROC, glTextureBarrier);

	GET_PROC_ADDRESS(PFNGLTEXSTORAGE3DPROC, glTexStorage3D);
	GET_PROC_ADDRESS(PFNGLTEXSTORAGE2DPROC, glTexStorage2D);


// arb programs
	GET_PROC_ADDRESS(PFNGLGENPROGRAMSARBPROC,glGenProgramsARB);
	GET_PROC_ADDRESS(PFNGLBINDPROGRAMARBPROC,glBindProgramARB);
	GET_PROC_ADDRESS(PFNGLDELETEPROGRAMSARBPROC,glDeleteProgramsARB);
	GET_PROC_ADDRESS(PFNGLPROGRAMSTRINGARBPROC,glProgramStringARB);
	GET_PROC_ADDRESS(PFNGLPROGRAMENVPARAMETER4FARBPROC,glProgramEnvParameter4fARB);
	GET_PROC_ADDRESS(PFNGLPROGRAMENVPARAMETER4FVARBPROC,glProgramEnvParameter4fvARB);
	GET_PROC_ADDRESS(PFNGLPROGRAMLOCALPARAMETER4FARBPROC,glProgramLocalParameter4fARB);
	GET_PROC_ADDRESS(PFNGLPROGRAMLOCALPARAMETER4FVARBPROC,glProgramLocalParameter4fvARB);

//shaders
	GET_PROC_ADDRESS(PFNGLATTACHSHADERPROC, glAttachShader);
	GET_PROC_ADDRESS(PFNGLBINDATTRIBLOCATIONPROC, glBindAttribLocation);
	GET_PROC_ADDRESS(PFNGLCOMPILESHADERPROC, glCompileShader);
	GET_PROC_ADDRESS(PFNGLCREATEPROGRAMPROC, glCreateProgram);
	GET_PROC_ADDRESS(PFNGLCREATESHADERPROC, glCreateShader);
	GET_PROC_ADDRESS(PFNGLDELETEPROGRAMPROC, glDeleteProgram);
	GET_PROC_ADDRESS(PFNGLDELETESHADERPROC, glDeleteShader);
	GET_PROC_ADDRESS(PFNGLDETACHSHADERPROC, glDetachShader);
	GET_PROC_ADDRESS(PFNGLSHADERSOURCEPROC, glShaderSource);
	GET_PROC_ADDRESS(PFNGLLINKPROGRAMPROC, glLinkProgram);
	GET_PROC_ADDRESS(PFNGLUSEPROGRAMPROC, glUseProgram);
	GET_PROC_ADDRESS(PFNGLPROGRAMUNIFORM1IPROC, glProgramUniform1i);

	GET_PROC_ADDRESS(PFNGLVALIDATEPROGRAMPROC, glValidateProgram);
	GET_PROC_ADDRESS(PFNGLGETACTIVEATTRIBPROC, glGetActiveAttrib);
	GET_PROC_ADDRESS(PFNGLGETACTIVEUNIFORMPROC, glGetActiveUniform);


	//uniform buffer
	GET_PROC_ADDRESS(PFNGLUNIFORMBUFFEREXTPROC,glUniformBufferEXT);
	//GET_PROC_ADDRESS(PFNGLGETUNIFORMLOCATIONARBPROC,glGetUniformLocationARB);
	GET_PROC_ADDRESS(PFNGLUNIFORMBLOCKBINDINGPROC, glUniformBlockBinding);
	GET_PROC_ADDRESS(PFNGLGETUNIFORMBLOCKINDEXPROC, glGetUniformBlockIndex);

	//texture buffer
	GET_PROC_ADDRESS(PFNGLTEXBUFFERARBPROC,glTexBufferARB);

	//glPrimitiveRestartIndex
	GET_PROC_ADDRESS(PFNGLPRIMITIVERESTARTINDEXPROC,glPrimitiveRestartIndex);
	GET_PROC_ADDRESS(PFNGLPRIMITIVERESTARTINDEXNVPROC,glPrimitiveRestartIndexNV);
	
//instancing
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSINSTANCEDEXTPROC,glDrawElementsInstancedEXT);
	GET_PROC_ADDRESS(PFNGLDRAWARRAYSINSTANCEDPROC, glDrawArraysInstanced);
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSINSTANCEDPROC, glDrawElementsInstanced);
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSINSTANCEDBASEINSTANCEPROC, glDrawElementsInstancedBaseInstance);
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXBASEINSTANCEPROC, glDrawElementsInstancedBaseVertexBaseInstance);
	GET_PROC_ADDRESS(PFNGLMULTIDRAWELEMENTSINDIRECTPROC, glMultiDrawElementsIndirect);
	GET_PROC_ADDRESS(PFNGLDRAWARRAYSINDIRECTPROC, glDrawArraysIndirect);

//draw
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSBASEVERTEXPROC, glDrawElementsBaseVertex);
	GET_PROC_ADDRESS(PFNGLDRAWRANGEELEMENTSBASEVERTEXPROC, glDrawRangeElementsBaseVertex);
	GET_PROC_ADDRESS(PFNGLDRAWELEMENTSINSTANCEDBASEVERTEXPROC, glDrawElementsInstancedBaseVertex);
	GET_PROC_ADDRESS(PFNGLMULTIDRAWELEMENTSBASEVERTEXPROC, glMultiDrawElementsBaseVertex);


	GET_PROC_ADDRESS(PFNGLTEXBUFFERPROC, glTexBuffer);
	//GET_PROC_ADDRESS(PFNGLPRIMITIVERESTARTINDEXPROC, glPrimitiveRestartIndex);
	GET_PROC_ADDRESS(PFNGLCOPYBUFFERSUBDATAPROC, glCopyBufferSubData);

// nv programs
	GET_PROC_ADDRESS(PFNGLGENPROGRAMSNVPROC,glGenProgramsNV);
	GET_PROC_ADDRESS(PFNGLBINDPROGRAMNVPROC,glBindProgramNV);
	GET_PROC_ADDRESS(PFNGLDELETEPROGRAMSNVPROC,glDeleteProgramsNV);
	GET_PROC_ADDRESS(PFNGLLOADPROGRAMNVPROC,glLoadProgramNV);
	GET_PROC_ADDRESS(PFNGLPROGRAMNAMEDPARAMETER4FNVPROC,glProgramNamedParameter4fNV);
	GET_PROC_ADDRESS(PFNGLPROGRAMNAMEDPARAMETER4FVNVPROC,glProgramNamedParameter4fvNV);
	
// attrib arrays
	GET_PROC_ADDRESS(PFNGLENABLEVERTEXATTRIBARRAYARBPROC,glEnableVertexAttribArrayARB);
	GET_PROC_ADDRESS(PFNGLVERTEXATTRIBPOINTERARBPROC,glVertexAttribPointerARB);
	GET_PROC_ADDRESS(PFNGLDISABLEVERTEXATTRIBARRAYARBPROC,glDisableVertexAttribArrayARB);
	GET_PROC_ADDRESS(PFNGLVERTEXATTRIB4FVARBPROC,glVertexAttrib4fvARB);
	GET_PROC_ADDRESS(PFNGLGETATTRIBLOCATIONARBPROC,glGetAttribLocation);

	GET_PROC_ADDRESS(PFNGLVERTEXATTRIBPOINTERPROC, glVertexAttribPointer);
	GET_PROC_ADDRESS(PFNGLENABLEVERTEXATTRIBARRAYPROC, glEnableVertexAttribArray);
	GET_PROC_ADDRESS(PFNGLDISABLEVERTEXATTRIBARRAYPROC, glDisableVertexAttribArray);
	GET_PROC_ADDRESS(PFNGLVERTEXATTRIBDIVISORPROC, glVertexAttribDivisor);

// vertex buffer object
	GET_PROC_ADDRESS(PFNGLGENBUFFERSARBPROC,glGenBuffersARB);
	GET_PROC_ADDRESS(PFNGLBINDBUFFERARBPROC,glBindBufferARB);
	GET_PROC_ADDRESS(PFNGLBUFFERDATAARBPROC,glBufferDataARB);
	GET_PROC_ADDRESS(PFNGLDELETEBUFFERSARBPROC,glDeleteBuffersARB);

	GET_PROC_ADDRESS(PFNGLDRAWRANGEELEMENTSPROC,glDrawRangeElements);
	GET_PROC_ADDRESS(PFNGLMAPBUFFERARBPROC,glMapBufferARB);
	GET_PROC_ADDRESS(PFNGLUNMAPBUFFERARBPROC,glUnmapBufferARB);

	GET_PROC_ADDRESS(PFNGLBINDBUFFERRANGEPROC, glBindBufferRange);
	GET_PROC_ADDRESS(PFNGLBINDBUFFERBASEPROC, glBindBufferBase);
	GET_PROC_ADDRESS(PFNGLBINDIMAGETEXTUREPROC, glBindImageTexture);


	GET_PROC_ADDRESS(PFNGLBINDBUFFERPROC, glBindBuffer);
	GET_PROC_ADDRESS(PFNGLDELETEBUFFERSPROC, glDeleteBuffers);
	GET_PROC_ADDRESS(PFNGLGENBUFFERSPROC, glGenBuffers);
	GET_PROC_ADDRESS(PFNGLBUFFERDATAPROC, glBufferData);

	GET_PROC_ADDRESS(PFNGLMAPBUFFERPROC, glMapBuffer);
	GET_PROC_ADDRESS(PFNGLUNMAPBUFFERPROC, glUnmapBuffer);
	GET_PROC_ADDRESS(PFNGLMAPBUFFERRANGEPROC, glMapBufferRange);


// vertex array
	GET_PROC_ADDRESS(PFNGLBINDVERTEXARRAYPROC, glBindVertexArray);
	GET_PROC_ADDRESS(PFNGLDELETEVERTEXARRAYSPROC, glDeleteVertexArray);
	GET_PROC_ADDRESS(PFNGLGENVERTEXARRAYSPROC, glGenVertexArrays);


//transform feedback
	GET_PROC_ADDRESS(PFNGLBINDTRANSFORMFEEDBACKPROC, glBindTransformFeedback);
	GET_PROC_ADDRESS(PFNGLDELETETRANSFORMFEEDBACKSPROC, glDeleteTransformFeedback);
	GET_PROC_ADDRESS(PFNGLGENTRANSFORMFEEDBACKSPROC, glGenTransformFeedback);
	GET_PROC_ADDRESS(PFNGLBEGINTRANSFORMFEEDBACKPROC, glBeginTransformFeedback);
	GET_PROC_ADDRESS(PFNGLENDTRANSFORMFEEDBACKPROC, glEndTransformFeedback);
	GET_PROC_ADDRESS(PFNGLTRANSFORMFEEDBACKVARYINGSPROC, glTransformFeedbackVaryings);
	GET_PROC_ADDRESS(PFNGLTRANSFORMFEEDBACKATTRIBSNVPROC, glTransformFeedbackAttribsNV);

	GET_PROC_ADDRESS(PFNGLBINDBUFFERBASENVPROC, glBindBufferBaseNV);
	GET_PROC_ADDRESS(PFNGLBEGINTRANSFORMFEEDBACKNVPROC, glBeginTransformFeedbackNV);
	GET_PROC_ADDRESS(PFNGLENDTRANSFORMFEEDBACKNVPROC, glEndTransformFeedbackNV);
	GET_PROC_ADDRESS(PFNGLBINDBUFFEROFFSETNVPROC, glBindBufferOffsetNV);
	GET_PROC_ADDRESS(PFNGLACTIVEVARYINGNVPROC, glActiveVaryingNV);
	GET_PROC_ADDRESS(PFNGLGETVARYINGLOCATIONNVPROC, glGetVaryingLocationNV);
	GET_PROC_ADDRESS(PFNGLTRANSFORMFEEDBACKVARYINGSNVPROC, glTransformFeedbackVaryingsNV);

	
// occlision query
	GET_PROC_ADDRESS(PFNGLGENQUERIESARBPROC, glGenQueriesARB);
	GET_PROC_ADDRESS(PFNGLDELETEQUERIESARBPROC, glDeleteQueriesARB);
	GET_PROC_ADDRESS(PFNGLBEGINQUERYARBPROC, glBeginQueryARB);
	GET_PROC_ADDRESS(PFNGLENDQUERYARBPROC, glEndQueryARB);
	GET_PROC_ADDRESS(PFNGLGETQUERYIVARBPROC, glGetQueryivARB);
	GET_PROC_ADDRESS(PFNGLGETQUERYOBJECTIVARBPROC, glGetQueryObjectivARB);
	GET_PROC_ADDRESS(PFNGLGETQUERYOBJECTUIVARBPROC, glGetQueryObjectuivARB);
	GET_PROC_ADDRESS(PFNGLGETQUERYOBJECTUI64VPROC, glGetQueryObjectui64v);
	GET_PROC_ADDRESS(PFNGLQUERYCOUNTERPROC, glQueryCounter);
	GET_PROC_ADDRESS(PFNGLBEGINQUERYINDEXEDPROC, glBeginQueryIndexed);
	GET_PROC_ADDRESS(PFNGLENDQUERYINDEXEDPROC, glEndQueryIndexed);
	GET_PROC_ADDRESS(PFNGLGETQUERYINDEXEDIVPROC, glGetQueryIndexediv);

	GET_PROC_ADDRESS(PFNGLGENQUERIESPROC, glGenQueries);
	GET_PROC_ADDRESS(PFNGLDELETEQUERIESPROC, glDeleteQueries);
	GET_PROC_ADDRESS(PFNGLBEGINQUERYPROC, glBeginQuery);
	GET_PROC_ADDRESS(PFNGLENDQUERYPROC, glEndQuery);
	GET_PROC_ADDRESS(PFNGLGETQUERYOBJECTIVPROC, glGetQueryObjectiv);


// glsl
	GET_PROC_ADDRESS(PFNGLCREATEPROGRAMOBJECTARBPROC,glCreateProgramObjectARB);
	GET_PROC_ADDRESS(PFNGLCREATESHADEROBJECTARBPROC,glCreateShaderObjectARB);
	GET_PROC_ADDRESS(PFNGLSHADERSOURCEARBPROC,glShaderSourceARB);
	GET_PROC_ADDRESS(PFNGLCOMPILESHADERARBPROC,glCompileShaderARB);
	GET_PROC_ADDRESS(PFNGLATTACHOBJECTARBPROC,glAttachObjectARB);
	GET_PROC_ADDRESS(PFNGLDETACHOBJECTARBPROC,glDetachObjectARB);
	GET_PROC_ADDRESS(PFNGLDELETEOBJECTARBPROC,glDeleteObjectARB);
	GET_PROC_ADDRESS(PFNGLBINDATTRIBLOCATIONARBPROC,glBindAttribLocationARB);
	GET_PROC_ADDRESS(PFNGLLINKPROGRAMARBPROC,glLinkProgramARB);
	GET_PROC_ADDRESS(PFNGLGETINFOLOGARBPROC,glGetInfoLogARB);
	GET_PROC_ADDRESS(PFNGLGETOBJECTPARAMETERIVARBPROC,glGetObjectParameterivARB);
	GET_PROC_ADDRESS(PFNGLVALIDATEPROGRAMARBPROC,glValidateProgramARB);
	GET_PROC_ADDRESS(PFNGLUSEPROGRAMOBJECTARBPROC,glUseProgramObjectARB);
	GET_PROC_ADDRESS(PFNGLGETUNIFORMLOCATIONARBPROC,glGetUniformLocationARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM1FARBPROC,glUniform1fARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM1FVARBPROC,glUniform1fvARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM2FARBPROC,glUniform2fARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM2FVARBPROC,glUniform2fvARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM3FARBPROC,glUniform3fARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM3FVARBPROC,glUniform3fvARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM4FARBPROC,glUniform4fARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM4FVARBPROC,glUniform4fvARB);

	GET_PROC_ADDRESS(PFNGLUNIFORMMATRIX3FVARBPROC,glUniformMatrix3fvARB);
	GET_PROC_ADDRESS(PFNGLUNIFORMMATRIX4FVARBPROC,glUniformMatrix4fvARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM3IVARBPROC,glUniform3ivARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM4IVARBPROC,glUniform4ivARB);
	GET_PROC_ADDRESS(PFNGLUNIFORM2IPROC, glUniform2i);
	
	
	GET_PROC_ADDRESS(PFNGLUNIFORMMATRIX2FVPROC, glUniformMatrix2fv);
	GET_PROC_ADDRESS(PFNGLUNIFORMMATRIX3FVPROC, glUniformMatrix3fv);
	GET_PROC_ADDRESS(PFNGLUNIFORMMATRIX4FVPROC, glUniformMatrix4fv);

	GET_PROC_ADDRESS(PFNGLGETSHADERINFOLOGPROC,glGetShaderInfoLog);
	GET_PROC_ADDRESS(PFNGLGETPROGRAMINFOLOGPROC,glGetProgramInfoLog);
	GET_PROC_ADDRESS(PFNGLGETSHADERIVPROC,glGetShaderiv);
	GET_PROC_ADDRESS(PFNGLGETPROGRAMIVPROC,glGetProgramiv);

	GET_PROC_ADDRESS(PFNGLGETUNIFORMLOCATIONPROC, glGetUniformLocation);
	GET_PROC_ADDRESS(PFNGLUNIFORM1IPROC, glUniform1i);
	
	GET_PROC_ADDRESS(PFNGLUNIFORM1FPROC, glUniform1f);
	GET_PROC_ADDRESS(PFNGLUNIFORM2FPROC, glUniform2f);
	GET_PROC_ADDRESS(PFNGLUNIFORM3FPROC, glUniform3f);
	GET_PROC_ADDRESS(PFNGLUNIFORM4FPROC, glUniform4f);


	GET_PROC_ADDRESS(PFNGLUNIFORM1FVPROC, glUniform1fv);
	GET_PROC_ADDRESS(PFNGLUNIFORM2FVPROC, glUniform2fv);
	GET_PROC_ADDRESS(PFNGLUNIFORM3FVPROC, glUniform3fv);
	GET_PROC_ADDRESS(PFNGLUNIFORM4FVPROC, glUniform4fv);
	GET_PROC_ADDRESS(PFNGLUNIFORM4IVPROC, glUniform4iv);

	GET_PROC_ADDRESS(PFNGLACTIVETEXTUREPROC, glActiveTexture);


//texture sampler
	GET_PROC_ADDRESS(PFNGLGENSAMPLERSPROC, glGenSamplers);
	GET_PROC_ADDRESS(PFNGLDELETESAMPLERSPROC, glDeleteSamplers);
	GET_PROC_ADDRESS(PFNGLBINDSAMPLERPROC, glBindSampler);
	GET_PROC_ADDRESS(PFNGLSAMPLERPARAMETERIPROC, glSamplerParameteri);
	GET_PROC_ADDRESS(PFNGLSAMPLERPARAMETERFVPROC, glSamplerParameterfv);


// GL_EXT_texture3D
	GET_PROC_ADDRESS(PFNGLTEXIMAGE3DEXTPROC, glTexImage3DEXT);


// EXT_depth_bounds_test
	GET_PROC_ADDRESS(PFNGLDEPTHBOUNDSEXTPROC, glDepthBoundsEXT);
	GET_PROC_ADDRESS(PFNGLISRENDERBUFFEREXTPROC, glIsRenderbufferEXT);
	GET_PROC_ADDRESS(PFNGLBINDRENDERBUFFEREXTPROC, glBindRenderbufferEXT);
	GET_PROC_ADDRESS(PFNGLDELETERENDERBUFFERSEXTPROC, glDeleteRenderbuffersEXT);
	GET_PROC_ADDRESS(PFNGLGENRENDERBUFFERSEXTPROC, glGenRenderbuffersEXT);
	GET_PROC_ADDRESS(PFNGLRENDERBUFFERSTORAGEEXTPROC, glRenderbufferStorageEXT);
	GET_PROC_ADDRESS(PFNGLGETRENDERBUFFERPARAMETERIVEXTPROC, glGetRenderbufferParameterivEXT);
	GET_PROC_ADDRESS(PFNGLISFRAMEBUFFEREXTPROC, glIsFramebufferEXT);
	GET_PROC_ADDRESS(PFNGLDELETEFRAMEBUFFERSEXTPROC, glDeleteFramebuffersEXT);
	GET_PROC_ADDRESS(PFNGLFRAMEBUFFERTEXTURE1DEXTPROC, glFramebufferTexture1DEXT);
	GET_PROC_ADDRESS(PFNGLFRAMEBUFFERTEXTURE3DEXTPROC, glFramebufferTexture3DEXT);
	GET_PROC_ADDRESS(PFNGLFRAMEBUFFERRENDERBUFFEREXTPROC, glFramebufferRenderbufferEXT);
	GET_PROC_ADDRESS(PFNGLGETFRAMEBUFFERATTACHMENTPARAMETERIVEXTPROC, glGetFramebufferAttachmentParameterivEXT);
	GET_PROC_ADDRESS(PFNGLGENERATEMIPMAPEXTPROC, glGenerateMipmapEXT);
	GET_PROC_ADDRESS(PFNGLFRAMEBUFFERTEXTURELAYEREXTPROC, glFramebufferTextureLayerEXT);

	GET_PROC_ADDRESS(PFNGLDRAWBUFFERSPROC, glDrawBuffers);
	GET_PROC_ADDRESS(PFNGLDRAWBUFFERSATIPROC, glDrawBuffersATI);

	GET_PROC_ADDRESS(PFNGLPROVOKINGVERTEXPROC, glProvokingVertex);

	GET_PROC_ADDRESS(PFNGLGENFRAMEBUFFERSPROC, glGenFramebuffers);
	GET_PROC_ADDRESS(PFNGLBINDFRAMEBUFFERPROC, glBindFramebuffer);
	GET_PROC_ADDRESS(PFNGLFRAMEBUFFERTEXTURE2DPROC, glFramebufferTexture2D);
	GET_PROC_ADDRESS(PFNGLCHECKFRAMEBUFFERSTATUSPROC, glCheckFramebufferStatus);
	GET_PROC_ADDRESS(PFNGLDELETEFRAMEBUFFERSPROC, glDeleteFramebuffers);

	GET_PROC_ADDRESS(PFNGLBINDFRAGDATALOCATIONPROC, glBindFragDataLocation);

//debug mode
	GET_PROC_ADDRESS(PFNGLDEBUGMESSAGECONTROLARBPROC, glDebugMessageControlARB);
	GET_PROC_ADDRESS(PFNGLDEBUGMESSAGEINSERTARBPROC, glDebugMessageInsertARB);
	GET_PROC_ADDRESS(PFNGLDEBUGMESSAGECALLBACKARBPROC, glDebugMessageCallbackARB);
	GET_PROC_ADDRESS(PFNGLGETDEBUGMESSAGELOGARBPROC, glGetDebugMessageLogARB);


//wgl
	GET_PROC_ADDRESS(PFNWGLCHOOSEPIXELFORMATARBPROC, wglChoosePixelFormatARB);
	GET_PROC_ADDRESS(PFNWGLCREATEPBUFFERARBPROC, wglCreatePbufferARB);
	GET_PROC_ADDRESS(PFNWGLGETPBUFFERDCARBPROC, wglGetPbufferDCARB);
	GET_PROC_ADDRESS(PFNWGLRELEASEPBUFFERDCARBPROC, wglReleasePbufferDCARB);
	GET_PROC_ADDRESS(PFNWGLDESTROYPBUFFERARBPROC, wglDestroyPbufferARB);
	GET_PROC_ADDRESS(PFNWGLQUERYPBUFFERARBPROC, wglQueryPbufferARB);
	GET_PROC_ADDRESS(PFNWGLBINDTEXIMAGEARBPROC, wglBindTexImageARB);
	GET_PROC_ADDRESS(PFNWGLRELEASETEXIMAGEARBPROC, wglReleaseTexImageARB);
	GET_PROC_ADDRESS(PFNWGLGETPIXELFORMATATTRIBIVARBPROC, wglGetPixelFormatAttribivARB);
	GET_PROC_ADDRESS(PFNWGLSETPBUFFERATTRIBARBPROC, wglSetPbufferAttribARB);
	GET_PROC_ADDRESS(PFNWGLGETEXTENSIONSSTRINGARBPROC, wglGetExtensionsStringARB);

#undef GET_PROC_ADDRESS
}
