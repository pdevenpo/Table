package edu.osu.table.ui.graph

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.osu.table.R

class GraphFragment : Fragment() {

    companion object {
        fun newInstance() = GraphFragment()
    }

    private lateinit var viewModel: GraphViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.graph_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GraphViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
