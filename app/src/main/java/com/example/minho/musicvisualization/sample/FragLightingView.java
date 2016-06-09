package com.example.minho.musicvisualization.sample;

import com.example.minho.musicvisualization.R;
import com.example.minho.musicvisualization.SampleView;
import com.example.minho.musicvisualization.renderer.FileLoader;
import com.example.minho.musicvisualization.renderer.TexData;

import java.io.InputStream;

public class FragLightingView extends SampleView {

	@Override
	public void OnInit()
	{
		String vs = FileLoader.ReadTxtFile(this, "shader/view_f_lit/f_lit.vs");
		String fs = FileLoader.ReadTxtFile(this, "shader/view_f_lit/f_lit.fs");
		mRenderer.SetProgram(vs, fs);

		InputStream teapot = FileLoader.GetStream(this, "obj3d/teapot");

		TexData[] textJ = new TexData[1];
		textJ[0] = FileLoader.ReadTexture(this, R.drawable.light_bokeh_texture_50);

		mRenderer.SetNewModel(teapot);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(25.0f, 25.0f, 25.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
