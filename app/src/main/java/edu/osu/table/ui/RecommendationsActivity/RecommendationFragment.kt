package edu.osu.table.ui.RecommendationsActivity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.osu.table.R

class RecommendationFragment : Fragment() {

    companion object {
        fun newInstance() = RecommendationFragment()
    }

    private lateinit var viewModel: RecommendationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.recommendation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecommendationViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
