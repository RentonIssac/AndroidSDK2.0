package kr.neolab.samplecode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import kr.neolab.samplecode.renderer.Renderer;
import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.metadata.MetadataCtrl;

public class SampleView extends View
{
	public enum ZoomFitType
	{
		FIT_SCREEN, FIT_WIDTH, FIT_HEIGHT
	}

	// paper background
	private Bitmap background = null;

	public Canvas canvas;
	// draw the strokes
	public ArrayList<Stroke> strokes = new ArrayList<Stroke>();

	private Stroke stroke = null;

	private int sectionId = 0, ownerId = 0, noteId = 0, pageId = 0;

	private float paper_scale = 11, offsetX = 0, offsetY = 0;
	private float paper_offsetX = 0, paper_offsetY = 0, paper_width = 0, paper_height = 0;

	private MetadataCtrl metadataCtrl = MetadataCtrl.getInstance();


	private ZoomFitType mZoomFitType = ZoomFitType.FIT_WIDTH;

	public SampleView( Context context )
	{
		super( context );

	}

	public void setPage( float width, float height,float dx, float dy ,String backImagePath)
	{
		if ( getWidth() <= 0 || getHeight() <= 0 || width <= 0 || height <= 0 )
		{
			return;
		}
		paper_offsetX = dx;
		paper_offsetY = dy;
		paper_width = width;
		paper_height = height;

		float width_ratio = getWidth() / paper_width;
		float height_ratio = getHeight() / paper_height;

		if(mZoomFitType == ZoomFitType.FIT_SCREEN)
			paper_scale = Math.min( width_ratio, height_ratio );
		else if(mZoomFitType == ZoomFitType.FIT_WIDTH)
			paper_scale = width_ratio;
		else
			paper_scale = height_ratio;

//		paper_scale = Math.max( width_ratio, height_ratio );

		int docWidth = (int) (paper_width * paper_scale);
		int docHeight = (int) (paper_height * paper_scale);

		int mw = getWidth() - docWidth;
		int mh = getHeight() - docHeight;

		if(mZoomFitType == ZoomFitType.FIT_SCREEN)
		{
			offsetX = mw / 2;
			offsetY = mh / 2;
		}
		else
		{
			offsetX = 0;
			offsetY = 0;

		}

		Bitmap temp_pdf3 = BitmapFactory.decodeFile( backImagePath );
		background = Bitmap.createScaledBitmap( temp_pdf3, docWidth, docHeight, true );

	}

	@Override
	public void draw( Canvas canvas )
	{
		super.draw( canvas );
		this.canvas = canvas;
		if(background == null)
		{
			canvas.drawColor( Color.LTGRAY );
		}
		else
		{
			int zoom_w = (int) ( paper_width * paper_scale );
			int zoom_h = (int) ( paper_height * paper_scale );
//					소스 렉트값 지정 및 타겟 렉트값 찾기
			Rect source = new Rect( 0, 0, background.getWidth(), background.getHeight() );
			RectF target = new RectF( offsetX, offsetY, offsetX + zoom_w, offsetY + zoom_h );
			canvas.drawBitmap( background, source, target, null );
		}

		if ( strokes != null && strokes.size() > 0 )
		{
			float screen_offset_x = -paper_offsetX * paper_scale;
			float screen_offset_y = -paper_offsetY * paper_scale;

			Renderer.draw( canvas, strokes.toArray( new Stroke[0] ), paper_scale, screen_offset_x+offsetX, screen_offset_y+offsetY,Stroke.STROKE_TYPE_PEN );
		}
	}

	public void addDot( Dot dot )
	{
		if ( this.sectionId != dot.sectionId || this.ownerId != dot.ownerId || this.noteId != dot.noteId || this.pageId != dot.pageId )
		{
			strokes = new ArrayList<Stroke>();

			this.sectionId = dot.sectionId;
			this.ownerId = dot.ownerId;
			this.noteId = dot.noteId;
			this.pageId = dot.pageId;
		}

		if ( DotType.isPenActionDown( dot.dotType ) || stroke == null || stroke.isReadOnly() )
		{
			stroke = new Stroke( sectionId, ownerId, noteId, pageId, dot.color );
			strokes.add( stroke );
		}

		stroke.add( dot );

		invalidate();
	}

	public void addStrokes( Stroke[] strs )
	{
		for ( Stroke stroke : strs )
		{
			strokes.add( stroke );
		}
		invalidate();
	}

	public void setBackgroundImage(Bitmap bitmap)
	{

	}

	public void changePage(int sectionId, int ownerId, int noteId, int pageId)
	{
		strokes.clear();

		float width = metadataCtrl.getPageWidth( noteId, pageId );
		float height = metadataCtrl.getPageHeight( noteId, pageId );

		float dx = metadataCtrl.getPageMarginLeft( noteId, pageId );
		float dy = metadataCtrl.getPageMarginRight( noteId, pageId );

		String imagePath = Const.SAMPLE_FOLDER_PATH + File.separator + ""+sectionId+"_"+ownerId+"_"+noteId+"_"+pageId+".jpg";

		setPage( width, height,dx, dy , imagePath);
		invalidate();
	}

	public class SampleThread extends Thread
	{
		private SurfaceHolder surfaceholder;
		private SampleView mSampleiView;
		private boolean running = false;

		public SampleThread( SurfaceHolder surfaceholder, SampleView mView )
		{
			this.surfaceholder = surfaceholder;
			this.mSampleiView = mView;
		}

		public void setRunning( boolean run )
		{
			running = run;
		}

		@Override
		public void run()
		{
			setName( "SampleThread" );

			Canvas mCanvas;

			while ( running )
			{
				mCanvas = null;

				try
				{
					mCanvas = surfaceholder.lockCanvas(); // lock canvas

					synchronized ( surfaceholder )
					{
						if ( mCanvas != null )
						{
							mSampleiView.draw( mCanvas );
						}
					}
				}
				finally
				{
					if ( mCanvas != null )
					{
						surfaceholder.unlockCanvasAndPost( mCanvas ); // unlock
						// canvas
					}
				}
			}
		}
	}
}
