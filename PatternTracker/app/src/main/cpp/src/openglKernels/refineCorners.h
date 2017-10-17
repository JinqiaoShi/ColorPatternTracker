static const char *refineCorners_kernel =
"#version 310 es\n"
"\n"
"#extension GL_ANDROID_extension_pack_es31a : require\n"
"layout(local_size_x = 4, local_size_y = 2) in;\n"
"layout(binding=1, rgba32f) uniform mediump writeonly image2D output_image;\n"
"layout(std430, binding = 2) buffer C_ssbo {int C[];};\n"
"layout(std430, binding = 3) buffer CNew_ssbo {int CNew[];};\n"
"void main()\n"
"{	\n"
"	int sz_x = int((gl_NumWorkGroups.x*gl_WorkGroupSize.x));\n"
"\t\tint sz_y = int((gl_NumWorkGroups.y*gl_WorkGroupSize.y));\n"
"	\n"
"\t\tint x = int(gl_GlobalInvocationID.x);\n"
"\t\tint y = int(gl_GlobalInvocationID.y);\n"
"	\t\t\n"
"\t\tint sz_win = 5;\n"
"\t\t\n"
"\t\tif(x<sz_win || x>=sz_x-sz_win || y<sz_win || y>=sz_y-sz_win)return;\n"
"\t\t\n"
"\t\tint id = y*sz_x+x;\n"
"\t\t\n"
"\t\tif(C[id]==0)return;\t\t\n"
"\t\t\n"
"\t\tint c;\n"
"\t\t\n"
"\t\tint mx=0;\n"
"\t\tint my=0;\n"
"\t\tint count=0;\n"
"\t\tfor(int i=-sz_win;i<sz_win;i++)\n"
"\t\t{\n"
"\t\t\t\tfor(int j=-sz_win;j<sz_win-1;j++){\n"
"\t\t\t\t\t\tid = (y+j)*sz_x+(x+i);\n"
"\t\t\t\t\t\tif(C[id]==1){\n"
"\t\t\t\t\t\t\t\tmx += (x+i);\n"
"\t\t\t\t\t\t\t\tmy += (y+j);\n"
"\t\t\t\t\t\t\t\tcount++;\n"
"\t\t\t\t\t\t}\n"
"\t\t\t\t}\n"
"\t\t}\n"
"\t\t\n"
"\t\tmx /= count;\n"
"\t\tmy /= count;\n"
"\t\t\n"
"\t\tx = mx;\n"
"\t\ty = my;\n"
"\t\t\n"
"\t\tid = y*sz_x+x;\n"
"\t\tCNew[id]=1;\n"
"	\n"
"	// if(CNew[id]==1){\n"
"		// ivec2 pos;\n"
"		// for(int i=-sz_win;i<sz_win;i++)\n"
"		// {\n"
"			// for(int j=-sz_win;j<sz_win-1;j++){\n"
"				// pos.x = x+i;\n"
"				// pos.y = y+j;\n"
"				// vec4 pixelfo = vec4(1.0f,1.0f,1.0f,1.0f);\n"
"				// imageStore(output_image, pos, pixelfo);					\n"
"			// }\n"
"		// }\n"
"	// }\n"
"}\n";
