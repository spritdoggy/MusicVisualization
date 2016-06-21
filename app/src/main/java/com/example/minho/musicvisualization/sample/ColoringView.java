package com.example.minho.musicvisualization.sample;

import com.example.minho.musicvisualization.SampleView;
import com.example.minho.musicvisualization.renderer.FileLoader;

import java.io.InputStream;

public class ColoringView extends SampleView {

	@Override
	public void OnInit()
	{
		String vs = FileLoader.ReadTxtFile(this, "shader/view/color.vs");
		String fs = FileLoader.ReadTxtFile(this, "shader/view/color.fs");
		mRenderer.SetProgram(vs, fs);

		InputStream chair = FileLoader.GetStream(this, "obj3d/chair.obj");

	//	TexData[] textJ = new TexData[1];
	//	textJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_c_brick);

		mRenderer.SetNewModel(chair);

	//	mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(25.0f, 25.0f, 25.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
