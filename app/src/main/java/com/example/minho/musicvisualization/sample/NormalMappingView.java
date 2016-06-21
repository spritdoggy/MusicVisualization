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
		String fs = FileLoader.ReadTxtFile(this, "shader/view/color.fs");
		String vs = FileLoader.ReadTxtFile(this, "shader/view/color.vs");
		mRenderer.SetProgram(vs, fs);

		InputStream teapot = FileLoader.GetStream(this, "obj3d/dummy_obj.obj");

		TexData[] textJ = new TexData[1];
		TexData[] normal_texJ = new TexData[1];
		textJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_c_brick);
		normal_texJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_n_brick);

		mRenderer.SetNewModel(teapot);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ);
		mRenderer.SetTexture(TexData.Type.TEXDATA_NORMAL_MAP, normal_texJ);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(25.0f, 25.0f, 25.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
