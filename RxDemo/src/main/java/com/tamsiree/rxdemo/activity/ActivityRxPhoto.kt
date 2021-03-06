package com.tamsiree.rxdemo.activity

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.tamsiree.rxdemo.R
import com.tamsiree.rxkit.RxBarTool.noTitle
import com.tamsiree.rxkit.RxDeviceTool.setPortrait
import com.tamsiree.rxkit.RxPhotoTool
import com.tamsiree.rxkit.RxPhotoTool.getImageAbsolutePath
import com.tamsiree.rxkit.RxSPTool.putContent
import com.tamsiree.rxui.activity.ActivityBase
import com.tamsiree.rxui.view.dialog.RxDialogChooseImage
import com.tamsiree.rxui.view.dialog.RxDialogScaleView
import com.tamsiree.rxui.view.dialog.RxDialogSureCancel
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kotlinx.android.synthetic.main.activity_rxphototool.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author tamsiree
 */
class ActivityRxPhoto : ActivityBase() {

    private var resultUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noTitle(this)
        setContentView(R.layout.activity_rxphototool)
        setPortrait(this)
    }

    override fun initView() {
        val r = mContext.resources
        resultUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(R.drawable.circle_elves_ball) + "/"
                + r.getResourceTypeName(R.drawable.circle_elves_ball) + "/"
                + r.getResourceEntryName(R.drawable.circle_elves_ball))
        rx_title.setLeftFinish(mContext)
        iv_avatar.setOnClickListener { initDialogChooseImage() }
        iv_avatar.setOnLongClickListener {
            //RxImageTool.showBigImageView(mContext, resultUri);
            val rxDialogScaleView = RxDialogScaleView(mContext, resultUri)
            rxDialogScaleView.show()
            false
        }
        btn_exit.setOnClickListener {
            val rxDialogSureCancel = RxDialogSureCancel(this)
            rxDialogSureCancel.cancelView.setOnClickListener { rxDialogSureCancel.cancel() }
            rxDialogSureCancel.sureView.setOnClickListener { finish() }
            rxDialogSureCancel.show()
        }
    }

    override fun initData() {

    }

    private fun initDialogChooseImage() {
        val dialogChooseImage = RxDialogChooseImage(mContext, resultUri)
        dialogChooseImage.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RxPhotoTool.GET_IMAGE_FROM_PHONE -> if (resultCode == Activity.RESULT_OK) {
//                    RxPhotoTool.cropImage(ActivityUser.this, );// ????????????
                initUCrop(data!!.data)
            }
            RxPhotoTool.GET_IMAGE_BY_CAMERA -> if (resultCode == Activity.RESULT_OK) {
                /* data.getExtras().get("data");*/
//                    RxPhotoTool.cropImage(ActivityUser.this, RxPhotoTool.imageUriFromCamera);// ????????????
                initUCrop(RxPhotoTool.imageUriFromCamera)
            }
            RxPhotoTool.CROP_IMAGE -> {
                val options = RequestOptions()
                        .placeholder(R.drawable.circle_elves_ball) //???????????????(???????????????????????????????????????)
                        .error(R.drawable.circle_elves_ball) //??????Glide??????????????????
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                Glide.with(mContext).load(RxPhotoTool.cropImageUri).apply(options).thumbnail(0.5f).into(iv_avatar)
            }
            UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {
                resultUri = UCrop.getOutput(data!!)
                roadImageView(resultUri, iv_avatar)
                putContent(mContext, "AVATAR", resultUri.toString())
            } else if (resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(data!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //???Uri??????????????? ??????????????????File????????????
    private fun roadImageView(uri: Uri?, imageView: ImageView?): File {
        val options = RequestOptions()
                .placeholder(R.drawable.circle_elves_ball) //???????????????(???????????????????????????????????????)
                .error(R.drawable.circle_elves_ball)
                .transform(CircleCrop()) //??????Glide??????????????????
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        Glide.with(mContext).load(uri).apply(options).thumbnail(0.5f).into(imageView!!)
        return File(getImageAbsolutePath(this, uri))
    }

    private fun initUCrop(uri: Uri?) {
        val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val time = System.currentTimeMillis()
        val imageName = timeFormatter.format(Date(time))
        val destinationUri = Uri.fromFile(File(cacheDir, "$imageName.jpeg"))
        val options = UCrop.Options()
        //????????????????????????????????????
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL)
        //???????????????????????????????????????
        //options.setHideBottomControls(true);
        //??????toolbar??????
        options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary))
        //?????????????????????
        options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimaryDark))

        //????????????
        //????????????????????????
        options.setMaxScaleMultiplier(5f)
        //???????????????????????????????????????
        options.setImageToCropBoundsAnimDuration(666)
        //?????????????????????????????????
        //options.setCircleDimmedLayer(true);
        //?????????????????????????????????
        // options.setShowCropFrame(false);
        //?????????????????????????????????
        //options.setCropGridStrokeWidth(20);
        //?????????????????????????????????
        //options.setCropGridColor(Color.GREEN);
        //?????????????????????
        //options.setCropGridColumnCount(2);
        //?????????????????????
        //options.setCropGridRowCount(1);
        UCrop.of(uri!!, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(this)
    }
}