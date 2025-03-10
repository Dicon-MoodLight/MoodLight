package com.example.moodlight.screen.main3

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.moodlight.R
import com.example.moodlight.api.ServerClient
import com.example.moodlight.database.UserData
import com.example.moodlight.databinding.FragmentMain3Binding
import com.example.moodlight.model.setting.SuccussChangePasswordModel
import com.example.moodlight.model.setting.UserModel
import com.example.moodlight.model.setting.UserUpdateModel
import com.example.moodlight.screen.MainActivity
import com.example.moodlight.screen.main3.qna.HelpAcitvity
import com.example.moodlight.screen.main3.setting.SettingActivity
import com.example.moodlight.util.GetTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat

class MainFragment3 : Fragment() {
    private var activity : MainActivity? = MainActivity()

    private lateinit var binding: FragmentMain3Binding
    private lateinit var bitmap : Bitmap
    private lateinit var userId : String
    private lateinit var userEmail : String
    private lateinit var user : UserData
    private lateinit var userNickName : String
    private val viewModel: Main3ViewModel by lazy {
        ViewModelProvider(this).get(Main3ViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main3, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.fragment = this

        (binding.wholeLayout as ViewGroup).layoutTransition.apply {
            val appearingAnimator = ObjectAnimator.ofFloat(view, "translationX", -1000f, 0f)
            val disappearingAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, 1000f)
            this.setAnimator(LayoutTransition.APPEARING, appearingAnimator)
            this.setAnimator(LayoutTransition.DISAPPEARING, disappearingAnimator)
            this.setStartDelay(LayoutTransition.APPEARING, 2000L)
            this.setDuration(LayoutTransition.APPEARING, 700L)
        }


        if (viewModel.email.value.equals("Loading"))
            Main3Helper.setAnimation(binding)
        else
            Main3Helper.setVisible(binding)
        binding.main3CommentSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val updateModel = UserUpdateModel(userNickName, userId, isChecked, viewModel.likeIsChecked.value!!)
            Main3Helper.setAlarm(viewModel, updateModel, requireActivity(), 0)
            viewModel.commentIsChecked.value = isChecked
        }

        binding.main3LikeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val updateModel = UserUpdateModel(userNickName, userId, viewModel.commentIsChecked.value!!, isChecked)
            Main3Helper.setAlarm(viewModel, updateModel, requireActivity(), 1)
            viewModel.likeIsChecked.value = isChecked
        }

        binding.main3Btn1.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SettingActivity::class.java)
                .putExtra("userId", userId)
                .putExtra("userEmail", userEmail)
                .putExtra("isPushCommentAlarm", viewModel.likeIsChecked.value)
                .putExtra("isPushLikeAlarm", viewModel.likeIsChecked.value), 100)
        }

        binding.main3Btn2.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SettingActivity::class.java)
                .putExtra("userId", userId)
                .putExtra("userEmail", userEmail)
                .putExtra("isPushAlarm", viewModel.likeIsChecked.value), 100)
        }

        binding.helpBtn1.setOnClickListener {
            //intent
            startActivity(Intent(requireActivity(), HelpAcitvity::class.java))
        }

        binding.main3Tv5.setOnClickListener {
            //intent
            startActivity(Intent(requireActivity(), HelpAcitvity::class.java))
        }

        binding.main3WithdrawalTv.setOnClickListener {
            activity?.onClickBtnInFragment(1)

        }
        binding.main3LogoutBtn.setOnClickListener {
            activity?.onClickBtnInFragment(2)
        }

        setUi()



        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            setUi()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setUi() {
        CoroutineScope(Dispatchers.IO).launch {
            ServerClient.getApiService().getUserInfo()
                .enqueue(object : retrofit2.Callback<UserModel>{
                    override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                        val it = response.body();
                        Log.d(TAG, "onResponse: result $it $response")
                        if(response.code() == 200){
                            Log.d(TAG, "onResponse: it time : ${it!!.created_date}")
                            setAnimation()
                            var sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ss'Z'")
                            var date = sf.parse(it.created_date)
                            var minus = GetTime.getTime(date.time)
                            userId = it.id
                            userEmail = it.email
                            userNickName = it.nickname
                            viewModel.username.value = it!!.nickname
                            viewModel.email.value = it.email
                            viewModel.main3Tv1Text.value = "무드등을 시작한지 ${minus}지났어요."
                            viewModel.subscription.value = GetTime.modifyJoinTime(date.time)
                            viewModel.likeIsChecked.value = it.usePushMessageOnLike
                            viewModel.commentIsChecked.value = it.usePushMessageOnComment
                        }
                    }

                    override fun onFailure(call: Call<UserModel>, t: Throwable) {
                        Toast.makeText(requireActivity(), "정보를 불러오는데에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun setAnimation(): Unit {
        binding.main3ProfileIv.postDelayed({
            binding.main3ProfileIv.isVisible = true
        }, 50L)

        binding.main3Tv1.postDelayed({
            binding.main3UserNameTv.isVisible = true
        }, 200L)
        binding.main3EmailTv.postDelayed({
            binding.main3EmailTv.isVisible = true
        }, 250L)
        binding.main3Tv1.postDelayed({
            binding.main3Tv1.isVisible = true
        }, 300L)
        binding.main3Btn1.postDelayed({
            binding.main3Btn1.isVisible = true
        }, 350L)
        binding.main3Btn2.postDelayed({
            binding.main3Btn1.isVisible = true
        }, 350L)
        binding.main3Tv2.postDelayed({
            binding.main3Tv2.isVisible = true
        }, 400L)
        binding.main3Tv3.postDelayed({
            binding.main3Tv3.isVisible = true
        }, 450L)
        binding.main3CommentSwitch.postDelayed({
            binding.main3CommentSwitch.isVisible = true
        }, 500L)
        binding.main3Tv4.postDelayed({
            binding.main3Tv4.isVisible = true
        }, 550L)
        binding.main3LikeSwitch.postDelayed({
            binding.main3LikeSwitch.isVisible = true
        }, 600L)
        binding.main3Tv5.postDelayed({
            binding.main3Tv5.isVisible = true
        }, 650L)
        binding.helpBtn1.postDelayed({
            binding.main3Tv5.isVisible = true
        }, 650L)
        binding.main3SubscriptionTv.postDelayed({
            binding.main3SubscriptionTv.isVisible = true
        }, 700L)
        binding.main3Tv6.postDelayed({
            binding.main3Tv6.isVisible = true
        }, 700L)
        binding.main3LogoutBtn.postDelayed({
            binding.main3LogoutBtn.isVisible = true
        }, 750L)
        binding.main3WithdrawalTv.postDelayed({
            binding.main3WithdrawalTv.isVisible = true
        }, 800L)

        binding.layout1.postDelayed({
            binding.layout1.isVisible = true
        }, 850L)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }
}