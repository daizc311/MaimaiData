package com.paperpig.maimaidata.ui.songdetail

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.paperpig.maimaidata.R
import com.paperpig.maimaidata.databinding.FragmentSongLevelBinding
import com.paperpig.maimaidata.model.Record
import com.paperpig.maimaidata.model.SongData
import com.paperpig.maimaidata.repository.ChartStatsManager
import com.paperpig.maimaidata.repository.SongDataManager
import com.paperpig.maimaidata.ui.BaseFragment
import com.paperpig.maimaidata.utils.Constants
import com.paperpig.maimaidata.utils.setCopyOnLongClick
import com.paperpig.maimaidata.utils.setShrinkOnTouch
import com.paperpig.maimaidata.utils.toDp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private const val ARG_PARAM1 = "songData"
private const val ARG_PARAM2 = "position"
private const val ARG_PARAM3 = "record"

/**
 * A simple [Fragment] subclass.
 * Use the [SongLevelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongLevelFragment : BaseFragment<FragmentSongLevelBinding>() {
    private lateinit var binding: FragmentSongLevelBinding
    private lateinit var songData: SongData
    private var record: Record? = null
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songData = it.getParcelable(ARG_PARAM1)!!
            position = it.getInt(ARG_PARAM2)
            record = it.getParcelable(ARG_PARAM3)
        }
    }


    override fun getViewBinding(container: ViewGroup?): FragmentSongLevelBinding {
        binding = FragmentSongLevelBinding.inflate(layoutInflater, container, false)
        return binding
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (record != null) {
            binding.chartStatusGroup.visibility = View.VISIBLE
            binding.chartNoStatusGroup.visibility = View.GONE
            binding.chartAchievement.text =
                getString(R.string.maimaidx_achievement_desc, record!!.achievements)
            binding.chartRank.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), record!!.getRankIcon())
            )
            binding.chartFcap.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), record!!.getFcIcon())
            )
            binding.chartFsfsd.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), record!!.getFsIcon())
            )
        } else {
            binding.chartStatusGroup.visibility = View.GONE
            binding.chartNoStatusGroup.visibility = View.VISIBLE

            binding.recordTips.setOnClickListener {
                Toast.makeText(context, R.string.no_record_tips, Toast.LENGTH_LONG).show()
            }
        }
        val statsList = ChartStatsManager.list
        val fitDiff =
            //宴会场不显示拟合定数
            //没有拟合定数数据显示为"-"
            if (songData.basic_info.genre == Constants.GENRE_UTAGE) {
                "-"
            } else {
                statsList[songData.id]?.get(position)?.fitDiff?.let {
                    BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toString()
                } ?: "-"
            }
        binding.songFitDiff.text = fitDiff


        val note = songData.charts[position].notes


        val breakTotal = note[note.size - 1]
        val totalScore = totalScore(note, songData.type == Constants.CHART_TYPE_DX)
        val format = DecimalFormat("0.#####%")

        format.roundingMode = RoundingMode.DOWN

        if (songData.old_ds.isNotEmpty() && position < songData.old_ds.size) {
            if (songData.old_ds[position] < songData.ds[position]) {
                binding.songLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mmd_color_red
                    )
                )
                binding.songLevel.text = getString(R.string.inner_level_up, songData.ds[position])
                binding.oldLevel.text =
                    getString(R.string.inner_level_old, songData.old_ds[position])
            } else if (songData.old_ds[position] > songData.ds[position]) {
                binding.songLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mmd_color_green
                    )
                )
                binding.songLevel.text = getString(R.string.inner_level_down, songData.ds[position])
                binding.oldLevel.text =
                    getString(R.string.inner_level_old, songData.old_ds[position])
            } else {
                binding.songLevel.text = "${songData.ds[position]}"
                binding.oldLevel.text =
                    getString(R.string.inner_level_old, songData.old_ds[position])
            }
        } else {
            binding.songLevel.text = songData.ds[position].toString()

        }

        binding.chartDesigner.apply {
            text = songData.charts[position].charter

            setShrinkOnTouch()
            setCopyOnLongClick(songData.charts[position].charter)
        }


        binding.chartView.setMaxValues(SongDataManager.getMaxNotesList())
        val noteValueList = listOf(
            (songData.charts[position].notes).sum(),
            songData.charts[position].notes[0],
            songData.charts[position].notes[1],
            songData.charts[position].notes[2],
            if (songData.type == Constants.CHART_TYPE_DX) songData.charts[position].notes[3] else 0,
            if (songData.type == Constants.CHART_TYPE_DX) songData.charts[position].notes[4] else songData.charts[position].notes[3]
        )
        binding.chartView.setValues(noteValueList)
        binding.chartView.setBarColor(songData.getBgColor())

        binding.tapGreatScore.text = format.format(1f / totalScore * 0.2)
        binding.tapGoodScore.text = format.format(1f / totalScore * 0.5)
        binding.tapMissScore.text = format.format(1f / totalScore)
        binding.holdGreatScore.text = format.format(2f / totalScore * 0.2)
        binding.holdGoodScore.text = format.format(2f / totalScore * 0.5)
        binding.holdMissScore.text = format.format(2f / totalScore)
        binding.slideGreatScore.text = format.format(3f / totalScore * 0.2)
        binding.slideGoodScore.text = format.format(3f / totalScore * 0.5)
        binding.slideMissScore.text = format.format(3f / totalScore)
        binding.breakGreat4xScore.text =
            format.format(5f / totalScore * 0.2 + (0.01 / breakTotal) * 0.6)
        binding.breakGreat3xScore.text =
            format.format(5f / totalScore * 0.4 + (0.01 / breakTotal) * 0.6)
        binding.breakGreat25xScore.text =
            format.format(5f / totalScore * 0.5 + (0.01 / breakTotal) * 0.6)
        binding.breakGoodScore.text =
            format.format(5f / totalScore * 0.6 + (0.01 / breakTotal) * 0.7)
        binding.breakMissScore.text = format.format(5f / totalScore + 0.01 / breakTotal)
        binding.break50Score.text = format.format(0.01 / breakTotal * 0.25)
        binding.break100Score.text = (format.format((0.01 / breakTotal) * 0.5))


        val notesAchievementStoke =
            (binding.noteAchievementLayout.background as LayerDrawable).findDrawableByLayerId(
                R.id.note_achievement_stroke
            ) as GradientDrawable
        val notesAchievementInnerStoke =
            (binding.noteAchievementLayout.background as LayerDrawable).findDrawableByLayerId(
                R.id.note_achievement_inner_stroke
            ) as GradientDrawable

        notesAchievementStoke.setStroke(
            4.toDp().toInt(),
            ContextCompat.getColor(requireContext(), songData.getStrokeColor())
        )

        notesAchievementInnerStoke.setStroke(
            3.toDp().toInt(), ContextCompat.getColor(
                requireContext(),
                songData.getBgColor()
            )
        )

        if (songData.type == Constants.CHART_TYPE_DX) {
            binding.finaleGroup.visibility = View.GONE
        } else {
            binding.finaleGroup.visibility = View.VISIBLE
            binding.finaleAchievement.text =
                String.format(
                    getString(R.string.maimai_achievement_format), BigDecimal(
                        (note[0] * 500 + note[1] * 1000 + note[2] * 1500 + note[3] * 2600) * 1.0 /
                                (note[0] * 500 + note[1] * 1000 + note[2] * 1500 + note[3] * 2500) * 100
                    ).setScale(2, BigDecimal.ROUND_DOWN)
                )
        }
    }

    private fun totalScore(note: List<Int>, isDx: Boolean): Int {
        return if (isDx) {
            (note[0] + note[3]) + note[1] * 2 + note[2] * 3 + note[4] * 5
        } else {
            note[0] + note[1] * 2 + note[2] * 3 + note[3] * 5
        }
    }

    companion object {
        fun newInstance(song: SongData, position: Int, record: Record?) =
            SongLevelFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, song)
                    putInt(ARG_PARAM2, position)
                    putParcelable(ARG_PARAM3, record)
                }
            }
    }
}