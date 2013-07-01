package me.alextsang.imagepicker;

import java.io.FileNotFoundException;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;

import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

	private static final int PICK_IMAGE = 0;

	@AfterViews
	void init() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == PICK_IMAGE) {
				Uri selectedImageUri = data.getData();
				ImageView v = (ImageView)findViewById(R.id.parent);

				try {
					Bitmap b = decodeUri(selectedImageUri);
					int orientation = getOrientation(this, selectedImageUri);
					Matrix matrix = new Matrix();
					matrix.postRotate(orientation);
					b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true); // rotating bitmap
					v.setImageBitmap(b);
				} catch (FileNotFoundException e) {
					// oh shit
					e.printStackTrace();
				}
			}
		}
	}

	// get orientation of a bitmap
	// http://stackoverflow.com/questions/8554264/how-to-use-exifinterface-with-a-stream-or-uri
	private static int getOrientation(Context context, Uri photoUri) {
		/* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		if (cursor.getCount() != 1) {
			return -1;
		}

		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	// load and scale down an image to prevent OOM errors
	// http://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app-in-android
	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 512;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE
					|| height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

	}
}