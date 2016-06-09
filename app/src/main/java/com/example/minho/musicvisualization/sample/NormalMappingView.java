package com.example.minho.musicvisualization.sample;

import com.example.minho.musicvisualization.R;
import com.example.minho.musicvisualization.SampleView;
import com.example.minho.musicvisualization.renderer.FileLoader;
import com.example.minho.musicvisualization.renderer.TexData;

import java.io.InputStream;

public class NormalMappingView extends SampleView {

	@Override
	public void OnInit()
	{
		String fs = FileLoader.ReadTxtFile(this, "shader/view_nor/nor.fs");
		String vs = FileLoader.ReadTxtFile(this, "shader/view_nor/nor.vs");
		mRenderer.SetProgram(vs, fs);

		InputStream teapot = FileLoader.GetStream(this, "obj3d/teapot");

		TexData[] textJ = new TexData[1];
		TexData[] normal_texJ = new TexData[1];
		textJ[0] = FileLoader.ReadTexture(this, R.drawable.light_bokeh_texture_50);
		normal_texJ[0] = FileLoader.ReadTexture(this, R.drawable.light_bokeh_texture_61);

		mRenderer.SetNewModel(teapot);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ);
		mRenderer.SetTexture(TexData.Type.TEXDATA_NORMAL_MAP, normal_texJ);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(25.0f, 25.0f, 25.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
