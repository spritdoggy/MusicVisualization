package com.medialab.android_gles_sample.renderer;


import android.opengl.GLES20;
import android.util.Log;

import com.medialab.android_gles_sample.joml.AxisAngle4f;
import com.medialab.android_gles_sample.joml.Matrix3f;
import com.medialab.android_gles_sample.joml.Matrix4f;
import com.medialab.android_gles_sample.joml.Quaternionf;
import com.medialab.android_gles_sample.joml.Vector2f;
import com.medialab.android_gles_sample.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class BasicRenderer {

	private static String TAG = "BasicRenderer";

	class VertexStrcut {
		public Vec3 pos;
		public Vec3 nor;
		public Vec3 tex;

	}

	public static int V_ATTRIB_POSITION = 0;
	public static int V_ATTRIB_NORMAL = 1;
	public static int V_ATTRIB_TEX = 2;
	public static int V_ATTRIB_TANGENT = 3;
	public static int V_ATTRIB_INSTPOSITION = 5;
	public static int TEX_POS_NORMAL = 6;
	public static int TEX_POS_CUBEMAP = 7;

	protected int mWidth;
	protected int mHeight;
	protected double mDeltaTime;

	BasicShader mShader;
	BasicCamera mCamera;

	boolean mIsAutoRotateEye;
	boolean mIsFill;

	static boolean mIsTouchOn;
	static Vector2f mTouchPoint;

	static Quaternionf startRotQuat;
	static Quaternionf lastRotQuat;
	static Vector2f ancPts;
	static boolean isUpdateAnc;

	// vertex buffer
	private FloatBuffer mVertexData;
	private ShortBuffer mIndices;
	private FloatBuffer mTangentData;

	int mVertexSize;
	int mIndexSize;
	int mTangentSize;

	// vertex buffer object and index buffer object
	int[] mVboVertices = {0};
	int[] mVboIndices = {0};
	int[] mVboTangents = {0};

	// variables for texture handling
	boolean mHasTexture;
	boolean mHasNorMap;

	// Texture object id
	int[] mTexId = {0};
	int[] mTexNorId = {0};

	public BasicRenderer() {
		mWidth = 0;
		mHeight = 0;
		mDeltaTime = 0;
		mIsAutoRotateEye = true;
		mIsFill = true;
		mIsTouchOn = false;
		mTouchPoint = new Vector2f(0);

		startRotQuat = new Quaternionf();
		lastRotQuat = startRotQuat;
		ancPts = new Vector2f(mTouchPoint);
		isUpdateAnc = false;

		mHasTexture = false;
		mHasNorMap = false;

		mIndexSize = 0;

		mCamera = new BasicCamera();
		mShader = new BasicShader();

	}

	public BasicCamera GetCamera() {
		return mCamera;
	}

	// Interface functions
/// Sets vertex shader and fragment shader for rendering
	public boolean SetProgram(String vertexSource, String fragmentSource) {
		mShader.CreateProgram(vertexSource, fragmentSource);

		if (mShader.GetProgram() == 0) {
			Log.e(TAG, "Could not create program.\n");
			return false;
		}

		mShader.Use();

		return true;
	}


	/****************************
	 * *** Interface functions ***
	 ****************************/
	public void SetNewModel(InputStream objSource) {
		ImportModel(objSource);
	}

	public void SetNewModel(InputStream objSource, float scale) {
		ImportModel(objSource);
	}

	public void SetTexture(TexData.Type type, TexData[] newTex) {
		switch (type) {
			case TEXDATA_GENERAL: // general texture
				Log.i(TAG, "Set Texture : general\n");
				mHasTexture = true;
				CreateTexBuffer(newTex[0], mTexId);
				break;
			case TEXDATA_NORMAL_MAP: // normal map
				Log.i(TAG, "Set Texture : normal map\n");
				mHasNorMap = true;
//				if (mTangentData.empty())
				ComputeTangent();
				CreateTexBuffer(newTex[0], mTexNorId);
				break;
			default:
				break;
		}
	}

	public boolean Initialize() {
		Log.i(TAG, "Initialize renderer.\n");
		LogInfo();

		CountTickInit();

		CreateVbo();
		SetState();

		return true;
	}

	public void SetViewPort(int w, int h) {
		Log.i(TAG, String.format("SetViewPort(%d, %d)\n", w, h));
		mWidth = w;
		mHeight = h;
		GLES20.glViewport(0, 0, w, h);
		BasicUtils.CheckGLerror("glViewport");

		mCamera.ComputePerspective(60.0f, w, h);
	}

	public void RenderFrame() {
		//Log.i(TAG, "RenderFrame()");
		ComputeTick();

		mDeltaTime = 0.01;

		//if (mIsAutoRotateEye) mCamera.RotateAuto(mDeltaTime);

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		BasicUtils.CheckGLerror("glClear");

		PassUniform();

		Draw();
	}

	/*****************************
	 * **** Texture functions *****
	 *****************************/
	void CreateTexBuffer(TexData newTex, int[] target) {
		Log.i(TAG, "CreateTexBuffer\n");
		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		BasicUtils.CheckGLerror("glPixelStorei");
		GLES20.glGenTextures(1, target, 0);
		BasicUtils.CheckGLerror("glGenTextures");

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, target[0]);
		BasicUtils.CheckGLerror("glBindTexture");

		TexBuffer(GLES20.GL_TEXTURE_2D, newTex);

		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		BasicUtils.CheckGLerror("glGenerateMipmap");

		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		BasicUtils.CheckGLerror("glTexParameteri");

		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		BasicUtils.CheckGLerror("glTexParameteri");

		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		BasicUtils.CheckGLerror("glTexParameteri");

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	void TexBuffer(int type, TexData newTex) {
		Log.i(TAG, "TexBuffer");

		GLES20.glTexImage2D(type, 0,
				newTex.format,
				newTex.width, newTex.height, 0,
				newTex.format,
				GLES20.GL_UNSIGNED_BYTE, newTex.pixels);

		BasicUtils.CheckGLerror("glTexImage2D");
	}

	/*******************************
	 * **** Rendering functions *****
	 *******************************/
	void SetState() {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	void CreateVbo() {
		Log.i(TAG, "CreateVbo\n");
		GLES20.glGenBuffers(1, mVboVertices, 0);
		GLES20.glGenBuffers(1, mVboIndices, 0);

		mVertexData.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboVertices[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
				mVertexSize * 4,
				mVertexData, GLES20.GL_STATIC_DRAW);

		mIndices.position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVboIndices[0]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
				mIndexSize * 2,
				mIndices,
				GLES20.GL_STATIC_DRAW);

		int stride = 4 * (3 + 3 + 2); // stride: sizeof(float) * number of components
		int offset = 0;
		GLES20.glEnableVertexAttribArray(V_ATTRIB_POSITION);
		GLES20.glVertexAttribPointer(V_ATTRIB_POSITION, 3, GLES20.GL_FLOAT, false, stride, offset);

		offset += 4 * 3;
		GLES20.glEnableVertexAttribArray(V_ATTRIB_NORMAL);
		GLES20.glVertexAttribPointer(V_ATTRIB_NORMAL, 3, GLES20.GL_FLOAT, false, stride, offset);

		// If renderer has texture, we should enable vertex attribute for texCoord
		if (mHasTexture || mHasNorMap) {
			offset += 4 * 3;
			GLES20.glEnableVertexAttribArray(V_ATTRIB_TEX);
			GLES20.glVertexAttribPointer(V_ATTRIB_TEX, 2, GLES20.GL_FLOAT, false, stride, offset);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexId[0]);
		}

		if (mHasNorMap) {
			// Bump mapping need to change space (world and TBN)
			// mTangentBuffer calculated by ComputeTangent() when normal texture has set
			GLES20.glGenBuffers(1, mVboTangents, 0);
			mTangentData.position(0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboTangents[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
					mTangentSize, mTangentData, GLES20.GL_STATIC_DRAW);

			offset = 0;
			stride = 4 * 3;
			GLES20.glEnableVertexAttribArray(V_ATTRIB_TANGENT);
			GLES20.glVertexAttribPointer(V_ATTRIB_TANGENT, 3, GLES20.GL_FLOAT, false, stride, offset);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TEX_POS_NORMAL);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexNorId[0]);
		}
	}

	Vector3f GetArcballVector(Vector2f point)
	{
		float radius = 1.0f;

		Vector3f P = new Vector3f(1.0f * point.x / mWidth * 2 - 1.0f,
						1.0f * point.y / mHeight * 2 - 1.0f,
						0);
		P.y = -P.y;

		float OP_squared = P.x * P.x + P.y * P.y;
		if (OP_squared <= radius * radius)
			P.z = (float)Math.sqrt(radius * radius - OP_squared); // Pythagore
		else
			P = P.normalize(); // nearest point

		return P;
	}

	float[] GetWorldMatrix()
	{
		float[] farray = new float[4*4];
		FloatBuffer fb = FloatBuffer.allocate(4 * 4);

		if (mIsTouchOn)
		{
			if (!isUpdateAnc)
			{
				ancPts.set(mTouchPoint);
				isUpdateAnc = true;
				Log.i(TAG, "Anchor Updated\n");
			}
			else
			{
				if (ancPts.x != mTouchPoint.x || ancPts.y != mTouchPoint.y)
				{
					// Get the vectors on the arcball
					Vector3f va = GetArcballVector(ancPts);
					Vector3f vb = GetArcballVector(mTouchPoint);

					// Get the rotation axis and the angle between the vector
					float angle = (float)Math.acos(Math.min(1.0f, va.dot(vb))) * 2.0f;

					Vector3f axisInCameraSpace = va.cross(vb).normalize();

					fb.put(GetCamera().GetViewMat());
					fb.position(0);
					Matrix4f cameraToObjectSpace = new Matrix4f(fb).invert();
					Vector3f axisInObjectSpace = new Matrix3f(cameraToObjectSpace).transform(axisInCameraSpace).normalize();

					Quaternionf curRotQuat = new Quaternionf(new AxisAngle4f(angle, axisInObjectSpace.x, axisInObjectSpace.y, axisInObjectSpace.z));
					lastRotQuat = curRotQuat.mul(startRotQuat).normalize();
				}
			}
		}
		else
		{
			startRotQuat = lastRotQuat;
			isUpdateAnc = false;
		}
		Matrix4f rotationMat = new Matrix4f();
		lastRotQuat.get(rotationMat);
		rotationMat.get(farray);

		return farray;
	}

	float[] GetInverseTranspose(float[] inArray) {
		FloatBuffer fb = FloatBuffer.allocate(4 * 4);
		fb.put(inArray);
		fb.position(0);
		Matrix4f m = new Matrix4f(fb);

		float[] outArray = new float[4 * 4];
		m.invert().transpose().get(outArray);

		return outArray;
	}

	void PassUniform() {
//		float[] worldMat = new float[16];
//		Matrix.setIdentityM(worldMat, 0);
		float[] worldMat = GetWorldMatrix();
		float[] viewMat = mCamera.GetViewMat();
		float[] projMat = mCamera.GetPerspectiveMat();

		mShader.SetUniform("worldMat", worldMat);
		mShader.SetUniform("viewMat", viewMat);
		mShader.SetUniform("projMat", projMat);
		mShader.SetUniform("invTransWorldMat", GetInverseTranspose(worldMat));
		mShader.SetUniform("s_tex0", 0);
		mShader.SetUniform("s_texNor", TEX_POS_NORMAL);
		mShader.SetUniform("eyePos", mCamera.GetEye());
		mShader.SetUniform("lightPos", 50.0f, 50.0f, 50.0f);
		mShader.SetUniform("materialDiff", 0.8f, 1.0f, 0.7f);
		mShader.SetUniform("materialSpec", 0.8f, 1.0f, 0.7f);
		mShader.SetUniform("materialAmbi", 0.0f, 0.0f, 0.0f);
		mShader.SetUniform("materialEmit", 0.0f, 0.0f, 0.0f);
		mShader.SetUniform("materialSh", 100.0f);
		mShader.SetUniform("sourceDiff", 0.7f, 0.7f, 0.7f);
		mShader.SetUniform("sourceSpec", 1.0f, 1.0f, 1.0f);
		mShader.SetUniform("sourceAmbi", 0.0f, 0.0f, 0.0f);
	}

	void Draw() {
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
		BasicUtils.CheckGLerror("glDrawElements");
	}


	/*****************************
	 * **** Utility functions *****
	 *****************************/
	void LogInfo() {
		BasicUtils.PrintGLstring("Version", GLES20.GL_VERSION);
		BasicUtils.PrintGLstring("Vendor", GLES20.GL_VENDOR);
		BasicUtils.PrintGLstring("Renderer", GLES20.GL_RENDERER);
		BasicUtils.PrintGLstring("Extensions", GLES20.GL_EXTENSIONS);
		BasicUtils.PrintGLstring("GLSLversion", GLES20.GL_SHADING_LANGUAGE_VERSION);
	}

	void CountTickInit() {
//		mTimer.InitTimer();
	}

	void ComputeTick() {
//		static double lastTick = 0;
//		double currTick = mTimer.GetElapsedTime();
//		mDeltaTime = currTick - lastTick;
//		lastTick = currTick;
		//Log.i(TAG, "Tick: %f\n", mDeltaTime);
	}

	void ComputeTangent() {
		int stride = 3 + 3 + 2;

		short[] indices = new short[mIndices.capacity()];
		mIndices.position(0);
		for (int i = 0; i < indices.length; ++i) {
			indices[i] = mIndices.get();
		}

		float[] vertices = new float[mVertexData.capacity()];
		mVertexData.position(0);
		for (int i = 0; i < vertices.length; ++i) {
			vertices[i] = mVertexData.get();
		}
		Vec3[] tangents = new Vec3[indices.length];

		// Compute Tangent Basis
		for (int i = 0; i < mIndices.capacity(); i += 3) {
			// Get triangle position
			Vec3 p0 = new Vec3(vertices[indices[i] * stride],
					vertices[indices[i] * stride + 1],
					vertices[indices[i] * stride + 2]);
			Vec3 p1 = new Vec3(vertices[indices[i + 1] * stride],
					vertices[indices[i + 1] * stride + 1],
					vertices[indices[i + 1] * stride + 2]);
			Vec3 p2 = new Vec3(vertices[indices[i + 2] * stride],
					vertices[indices[i + 2] * stride + 1],
					vertices[indices[i + 2] * stride + 2]);

			// Get triangle UV coordinate
			Vec3 uv0 = new Vec3(vertices[indices[i] * stride + 6],
					vertices[indices[i] * stride + 7], 0.0f);
			Vec3 uv1 = new Vec3(vertices[indices[i + 1] * stride + 6],
					vertices[indices[i + 1] * stride + 7], 0.0f);
			Vec3 uv2 = new Vec3(vertices[indices[i + 2] * stride + 6],
					vertices[indices[i + 2] * stride + 7], 0.0f);

			// Compute delta
			Vec3 deltaPos1 = Vec3.sub(p1, p0);
			Vec3 deltaPos2 = Vec3.sub(p2, p0);

			Vec3 deltaUV1 = Vec3.sub(uv1, uv0);
			Vec3 deltaUV2 = Vec3.sub(uv2, uv0);

			//Compute the tangent
			float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
			Vec3 tangent = Vec3.mul(deltaPos1, deltaUV2.y).sub(Vec3.mul(deltaPos2, deltaUV1.y)).mul(r);

			// Put in temp array
			tangents[i] = tangent;
			tangents[i + 1] = tangent;
			tangents[i + 2] = tangent;
		}

		// Initialize indTangents
		Vec3[] indTangents = new Vec3[vertices.length / stride];
		for (int i = 0; i < indTangents.length; ++i) {
			indTangents[i] = new Vec3();
		}

		// Accumulate tangents by indices
		for (int i = 0; i < tangents.length; i++) {
			indTangents[indices[i]].add(tangents[i]);
		}

		// Convert array to bytebuffer for glsl transaction
		mTangentData = ByteBuffer.allocateDirect(indTangents.length * 3 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < indTangents.length; i++) {
			mTangentData.put(indTangents[i].getArray());
		}
		mTangentSize = indTangents.length * 3 * 4;

	}

	public void ImportModel(InputStream obj) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(obj));

		String str;

		String name = "";
		List<Float> pos = new Vector<>();
		List<Float> normal = new Vector<>();
		List<Float> tex = new Vector<>();
		List<Float> vertex = new Vector<>();
		List<Short> indices = new Vector<>();
		HashMap<String, Short> elem = new HashMap<>();
		int posNumComponents = 0;
		int norNumComponents = 0;
		int texNumComponents = 0;

		try {
			while ((str = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str);
				if (st.hasMoreTokens()) {
					String curToken = st.nextToken();

					switch (curToken) {
						case "v":
							posNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								pos.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "vn":
							norNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								normal.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "vt":
							texNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								tex.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "f":
							while (st.hasMoreTokens()) {
								String element = st.nextToken();
								if (elem.containsKey(element)) {
									short index = elem.get(element);
									indices.add(index);
								} else {
									indices.add(((short) elem.size()));
									elem.put(element, (short) elem.size());
									String listIndex[];
									listIndex = element.split("/");
									for (int i = 0; i < posNumComponents; ++i) {
										int k = Integer.parseInt(listIndex[0]) - 1;
										vertex.add(pos.get(k * posNumComponents + i));
									}
									for (int i = 0; i < norNumComponents; ++i) {
										int k = Integer.parseInt(listIndex[2]) - 1;
										vertex.add(normal.get(k * norNumComponents + i));
									}
									for (int i = 0; i < texNumComponents - 1; ++i) {
										int k = Integer.parseInt(listIndex[1]) - 1;
										vertex.add(tex.get(k * texNumComponents + i));
									}
								}
							}
							break;

						case "g":
							name = st.nextToken();
							break;

						default:
							break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		mVertexData = ByteBuffer.allocateDirect(vertex.size() * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mIndices = ByteBuffer.allocateDirect(indices.size() * 2)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		for (int i = 0; i < vertex.size(); i++) mVertexData.put(vertex.get(i));
		for (int i = 0; i < indices.size(); i++) mIndices.put(indices.get(i));

		mVertexData.position(0);
		mIndices.position(0);
		mVertexSize = vertex.size();
		mIndexSize = indices.size();

	}

	public void TouchOn() {
		mIsTouchOn = true;
	}


	public void TouchOff() {
		mIsTouchOn = false;
	}

	public void SetTouchPoint(float x, float y) {
		mTouchPoint.x = x;
		mTouchPoint.y = y;
	}

}
