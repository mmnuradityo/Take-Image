package com.mmnuradityo.takelistimages.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.mmnuradityo.basestyle.base.BaseRvAdapter
import com.mmnuradityo.takelistimages.R
import com.mmnuradityo.takelistimages.model.data.DataImage
import com.mmnuradityo.takelistimages.ui.adapter.RvAdapter.Holder
import kotlinx.android.synthetic.main.item_image.view.*

class RvAdapter(private val context: Context) : BaseRvAdapter<DataImage, Holder>(context) {

    private var data = mutableListOf<DataImage>()
    private lateinit var view: RvAdapterView
    private var paddingInDp: Int = 0
    private var listSize: Int = 0
    private var size: Int = 0

    fun addDataList(data: MutableList<DataImage>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun setLayoutItem(): Int = R.layout.item_image

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val density = context.resources.displayMetrics.density
        this.paddingInDp = density.toInt()
        this.size = parent.measuredWidth / 4
        this.size = this.size - (this.paddingInDp * 2)
        parent.setPadding(paddingInDp, paddingInDp, paddingInDp, paddingInDp)
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun injectHolder(v: View): Holder = Holder(v)

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setComponent(view, size, paddingInDp)
        holder.bind(this.data[position], position)
    }

    fun setComponent(view: RvAdapterView) {
        this.view = view
    }

    fun setLimitList(listSize: Int) {
        this.listSize = listSize
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    class Holder(v: View) : BaseHolder<DataImage>(v), OnClickListener {

        companion object {
            var imageBtnActive = true
        }

        private var marginInDp: Int = 0
        private var lastIndex: Int = 5
        private lateinit var view: RvAdapterView
        private var size: Int = 0

        fun setComponent(
            view: RvAdapterView,
            size: Int,
            paddingInDp: Int
        ) {
            this.view = view
            this.size = size
            this.marginInDp = paddingInDp
        }

        override fun initView() = with(itemView) {
            when {
                getItemPosition() >= lastIndex -> {
                    this.visibility = View.GONE
                }
                getItemPosition() < lastIndex -> {
                    this.visibility = View.VISIBLE
                    initImage()
                }
            }
            this.setOnClickListener(this@Holder)
        }

        @SuppressLint("ResourceAsColor", "ResourceType", "NewApi")
        private fun initImage() = with(itemView) {
            val layoutParams = RelativeLayout.LayoutParams(size, size)
            layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp)
            this.layoutParams = layoutParams
            if (getData().uri == null) {
                val padding = (size / 6)
                image_item.layoutParams = layoutParams
                image_item.setPadding(padding, padding, padding, padding * 2)
                image_item.setBackgroundColor(android.R.color.transparent)
                this.background = ContextCompat.getDrawable(context, R.drawable.circle_ripple)
                tv_item.visibility = View.VISIBLE
                val density = context.resources.displayMetrics.density
                val paddingTv = (4 * density).toInt()
                val tvLayoutParams = RelativeLayout.LayoutParams(size, size)
                tvLayoutParams.setMargins(
                    paddingTv,
                    (size - (padding * 3)) + (paddingTv * 2),
                    paddingTv,
                    padding
                )
                tv_item.layoutParams = tvLayoutParams
            } else {
                image_item.layoutParams = layoutParams
                image_item.setPadding(0, 0, 0, 0)
                image_item.background = null
                image_item.setBackgroundColor(R.color.baseStyle_blackTransparent)
                this.background = null
                tv_item.visibility = View.GONE
            }
            getData()
            image_item.setImageBitmap(getData().bitmap)
        }

        override fun onClick(v: View?) {
            if (getData().uri == null) {
                view.onItemCameraClick()
            } else if (imageBtnActive) {
                imageBtnActive = false
                view.onItemImageClick(getData(), getItemPosition())
            }
        }
    }

}

interface RvAdapterView {
    fun onItemCameraClick()
    fun onItemImageClick(data: DataImage, position: Int)
}