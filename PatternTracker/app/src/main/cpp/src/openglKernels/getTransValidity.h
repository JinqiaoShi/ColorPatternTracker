static const char *getTransValidity_kernel =
"#version 310 es\n"
"\n"
"#extension GL_ANDROID_extension_pack_es31a : require\n"
"layout(local_size_x = 4, local_size_y = 2) in;\n"
"layout(binding=0, rgba32f) uniform mediump readonly image2D input_image;\n"
"layout(std430, binding = 2) buffer transValidity_ssbo {int validity[];};\n"
"layout(std430, binding = 3) buffer data_ssbo {float data[];};\n"
"layout(std430, binding = 4) buffer inVals_ssbo {float inVals[];};\n"
"void main()\n"
"{\n"
"	float x_mid, y_mid, angle, d_w, d_h;\n"
"	x_mid = inVals[0];\n"
"	y_mid = inVals[1];\n"
"	angle = inVals[2];\n"
"	d_w = inVals[3];\n"
"	d_h = inVals[4];\n"
"	\n"
"\t\tint id1 = int(float(gl_GlobalInvocationID.x));\n"
"\t\tint id2 = int(float(gl_GlobalInvocationID.y));\n"
"		\t\t\n"
"\t\t//int id = id1*18+id2;\n"
"\t\t//if(id>=405)return;\n"
"\t\tint id = id1*8+id2;\n"
"\t\tif(id>=125)return;\n"
"\t\t\n"
"\t\t//int nx_check = 4;\n"
"\t\t//int ny_check = 4;\n"
"\t\t//int nt_check = 2;\n"
"\t\t\n"
"\t\tint nx_check = 2;\n"
"\t\tint ny_check = 2;\n"
"\t\tint nt_check = 2;\n"
"\t\t\n"
"\t\tfloat dxt = (floor(float(id)/float((2*ny_check+1)*(2*nt_check+1)))-float(nx_check));\n"
"\t\tint id_temp = id%((2*ny_check+1)*(2*nt_check+1));\n"
"\t\tfloat dyt = (floor(float(id_temp)/float(2*nt_check+1))-float(ny_check));\n"
"\t\tfloat dt = float((id_temp%(2*nt_check+1))-nt_check);\n"
"\t\t\t\n"
"\t\tfloat dx = dxt*d_w*cos(angle)+dyt*d_h*sin(angle);\n"
"\t\tfloat dy = -dxt*d_w*sin(angle)+dyt*d_h*cos(angle);\n"
"\t\tdt = dt*3.142f/12.0f;\n"
"\t\t\n"
"\t\tfloat trans[2][2];\n"
"	trans[0][0] = cos(dt);\n"
"	trans[0][1] = -sin(dt);\n"
"	trans[1][0] = sin(dt);\n"
"	trans[1][1] = cos(dt);\n"
"	\n"
"\t\tivec2 pos;\n"
"\t\tvec4 pf;\n"
"\t\t\n"
"\t\t//uchar color[16] = {1,2,1,2,2,3,2,3,1,2,1,2,3,1,3,1};\n"
"\t\tint color[16] = int[16](1,3,1,2,2,1,2,3,3,2,3,1,1,3,1,2); // new pattern\n"
"\t\t\n"
"\t\tint flag=1;\n"
"\t\tfor(int i=0;i<16;i++){\n"
"\t\t\t\tfloat x = data[2*i]-x_mid;\n"
"\t\t\t\tfloat y = data[2*i+1]-y_mid;\n"
"\n"
"\t\t\t\tpos.y = int(floor(x*trans[0][0]+y*trans[0][1]+dx+x_mid+0.5f));\n"
"\t\t\t\tpos.x = int(floor(x*trans[1][0]+y*trans[1][1]+dy+y_mid+0.5f));\n"
"\t\t\t\t\n"
"\t\t\t\tif(pos.x<0 || pos.x>=1920 || pos.y<0 || pos.y>=1080){flag=0;break;}\n"
"\t\t\t\t\n"
"		pf = imageLoad(input_image, pos);\n"
"\t\t\t\t//pf = read_imagef(input_image, sampler, pos);\n"
"\t\t\t\tif(color[i]==1 && (pf.x<pf.y || pf.x<pf.z)){flag=0;break;}\n"
"\t\t\t\tif(color[i]==2 && (pf.y<pf.x || pf.y<pf.z)){flag=0;break;}\n"
"\t\t\t\tif(color[i]==3 && (pf.z<pf.y || pf.z<pf.x)){flag=0;break;}\n"
"\t\t}\n"
"\t\t\n"
"\t\t// check neighborhood\n"
"\t\tif(flag==1){\n"
"\t\t\t\tfloat d=d_h/4.0f;\n"
"\t\t\t\tif(d>d_w/4.0f)d=d_w/4.0f;\n"
"\t\t\t\td = floor(d);\n"
"\t\t\t\tif(d>2.0)d=2.0;\n"
"\t\t\t\tint iVec[4]=int[4](0, 3, 12, 5);\n"
"\t\t\t\tfor(int t=0;t<4;t++){\n"
"\t\t\t\t\t\tint i=iVec[t];\n"
"\t\t\t\t\t\tfloat x = data[2*i]-x_mid;\n"
"\t\t\t\t\t\tfloat y = data[2*i+1]-y_mid;\n"
"\t\t\t\t\t\t\n"
"\t\t\t\t\t\tfloat xt[4] = float[4](-d,-d, d, d);\n"
"\t\t\t\t\t\tfloat yt[4] = float[4](-d, d,-d, d);\n"
"\t\t\t\t\t\t\n"
"\t\t\t\t\t\tfor(int k=0;k<4;k++){\n"
"\t\t\t\t\t\t\t\tpos.y = int(floor((x+xt[k])*trans[0][0]+(y+yt[k])*trans[0][1]+dx+x_mid+0.5f));\n"
"\t\t\t\t\t\t\t\tpos.x = int(floor((x+xt[k])*trans[1][0]+(y+yt[k])*trans[1][1]+dy+y_mid+0.5f));\n"
"\t\t\t\t\t\t\t\t\n"
"\t\t\t\t\t\t\t\tif(pos.x<0 || pos.x>=1920 || pos.y<0 || pos.y>=1080){flag=0;break;}\n"
"\t\t\t\t\t\t\t\t\n"
"				pf = imageLoad(input_image, pos);\n"
"\t\t\t\t\t\t\t\t//pf = read_imagef(input_image, sampler, pos);\n"
"\t\t\t\t\t\t\t\tif(color[i]==1 && (pf.x<pf.y || pf.x<pf.z)){flag=0;break;}\n"
"\t\t\t\t\t\t\t\tif(color[i]==2 && (pf.y<pf.x || pf.y<pf.z)){flag=0;break;}\n"
"\t\t\t\t\t\t\t\tif(color[i]==3 && (pf.z<pf.y || pf.z<pf.x)){flag=0;break;}\n"
"\t\t\t\t\t\t}\n"
"\t\t\t\t\t\tif(flag==0)break;\n"
"\t\t\t\t}\n"
"\t\t}\n"
"\t\t\n"
"\t\tvalidity[id]=flag;\n"
"}\n";
