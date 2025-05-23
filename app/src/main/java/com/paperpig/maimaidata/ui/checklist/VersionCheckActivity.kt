package com.paperpig.maimaidata.ui.checklist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.paperpig.maimaidata.R
import com.paperpig.maimaidata.databinding.ActivityVersionCheckBinding
import com.paperpig.maimaidata.model.Record
import com.paperpig.maimaidata.model.SongData
import com.paperpig.maimaidata.model.Version
import com.paperpig.maimaidata.repository.RecordDataManager
import com.paperpig.maimaidata.repository.SongDataManager
import com.paperpig.maimaidata.utils.Constants
import com.paperpig.maimaidata.utils.SpUtil

class VersionCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVersionCheckBinding
    private var dataList = listOf<SongData>()
    private var recordList = listOf<Record>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVersionCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        supportActionBar?.title = getString(R.string.version_query)

        val versionList = getVersionList()
        val versionArrayAdapter =
            VersionArrayAdapter(
                this@VersionCheckActivity,
                R.layout.item_spinner_version,
                versionList
            )
        versionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val lastSelectedPosition = SpUtil.getLastQueryVersion()
        recordList = RecordDataManager.list
            //只获取master难度分数记录
            .filter { it.level_index == 3 }

        dataList = SongDataManager.list
        binding.versionSpn.apply {
            adapter = versionArrayAdapter
            setSelection(lastSelectedPosition, true)
            onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        SpUtil.saveLastQueryVersion(position)
                        val filter =
                            dataList.filter {
                                (it.basic_info.from == (parent?.getItemAtPosition(
                                    position
                                ) as Version).versionName) && it.basic_info.genre != Constants.GENRE_UTAGE
                            }

                        (binding.versionCheckRecycler.adapter as VersionCheckAdapter).updateData(
                            filter.sortedByDescending { it.ds[3] })
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                }

        }

        binding.versionCheckRecycler.apply {
            adapter =
                VersionCheckAdapter(context,
                    dataList.filter {
                        it.basic_info.from == versionList[lastSelectedPosition].versionName
                                && it.basic_info.genre != Constants.GENRE_UTAGE
                                && it.ds.size > 3
                    }.sortedByDescending { it.ds[3] }, recordList
                )

            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START // 设置主轴上的对齐方式为起始位置
            }
        }


        binding.switchBtn.setOnClickListener {
            (binding.versionCheckRecycler.adapter as VersionCheckAdapter).updateDisplay()
        }

    }

    private fun getVersionList(): MutableList<Version> {
        return mutableListOf(
            Version("maimai", R.drawable.maimai),
            Version("maimai PLUS", R.drawable.maimai_plus),
            Version("maimai GreeN", R.drawable.maimai_green),
            Version("maimai GreeN PLUS", R.drawable.maimai_green_plus),
            Version("maimai ORANGE", R.drawable.maimai_orange),
            Version("maimai ORANGE PLUS", R.drawable.maimai_orange),
            Version("maimai PiNK", R.drawable.maimai_pink),
            Version("maimai PiNK PLUS", R.drawable.maimai_pink_plus),
            Version("maimai MURASAKi", R.drawable.maimai_murasaki),
            Version("maimai MURASAKi PLUS", R.drawable.maimai_murasaki_plus),
            Version("maimai MiLK", R.drawable.maimai_milk),
            Version("maimai MiLK PLUS", R.drawable.maimai_milk_plus),
            Version("maimai FiNALE", R.drawable.maimai_finale),
            Version("舞萌DX", R.drawable.maimaidx_cn),
            Version("舞萌DX 2021", R.drawable.maimaidx_2021),
            Version("舞萌DX 2022", R.drawable.maimaidx_2022),
            Version("舞萌DX 2023", R.drawable.maimaidx_2023),
            Version("舞萌DX 2024", R.drawable.maimaidx_2024),
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
        }
        return true
    }

}