
package com.example.lab_exam_03.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lab_exam_03.LoginActivity
import com.example.lab_exam_03.R

class AccountFragment : Fragment() {
    
    private lateinit var tvAvatar: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnSignOut: Button
    private lateinit var btnChangeAvatar: TextView
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnChangePassword: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        
        // Initialize views
        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail)
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        etBio = view.findViewById(R.id.etBio)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)
        btnSignOut = view.findViewById(R.id.btnSignOut)
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar)
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmNewPassword = view.findViewById(R.id.etConfirmNewPassword)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        
        // Load user data
        loadUserData()
        
        // Save changes button
        btnSaveChanges.setOnClickListener {
            saveUserData()
        }
        
        // Sign out button
        btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
        
        // Change avatar button
        btnChangeAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "Avatar upload feature", Toast.LENGTH_SHORT).show()
        }
        
        // Change password button
        btnChangePassword.setOnClickListener {
            changePassword()
        }
        
        return view
    }
    
    private fun loadUserData() {
        val sharedPref = requireActivity().getSharedPreferences("WellnessApp", Context.MODE_PRIVATE)
        
        // Get user data
        val name = sharedPref.getString("user_name", "User")
        val email = sharedPref.getString("user_email", "user@wellness.app")
        val phone = sharedPref.getString("user_phone", "")
        val bio = sharedPref.getString("user_bio", "")
        
        // Display in profile header
        tvProfileName.text = name
        tvProfileEmail.text = email
        
        // Set avatar initials
        val initials = getInitials(name ?: "User")
        tvAvatar.text = initials
        
        // Fill form fields
        etFullName.setText(name)
        etEmail.setText(email)
        etPhone.setText(phone)
        etBio.setText(bio)
    }
    
    private fun saveUserData() {
        val name = etFullName.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()
        val bio = etBio.text.toString()
        
        // Validate
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Name and email are required", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save to SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("WellnessApp", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.putString("user_phone", phone)
        editor.putString("user_bio", bio)
        editor.apply()
        
        // Update profile header
        tvProfileName.text = name
        tvProfileEmail.text = email
        tvAvatar.text = getInitials(name)
        
        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun signOut() {
        // Go to Login page
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else {
            name.take(2).uppercase()
        }
    }
    
    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmNewPassword = etConfirmNewPassword.text.toString()
        
        // Validate fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all password fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get saved password
        val sharedPref = requireActivity().getSharedPreferences("WellnessApp", Context.MODE_PRIVATE)
        val savedPassword = sharedPref.getString("user_password", "")
        
        // Check if current password matches
        if (currentPassword != savedPassword) {
            Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate new password length
        if (newPassword.length < 8) {
            Toast.makeText(requireContext(), "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if new passwords match
        if (newPassword != confirmNewPassword) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if new password is different from current
        if (newPassword == currentPassword) {
            Toast.makeText(requireContext(), "New password must be different from current password", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save new password
        val editor = sharedPref.edit()
        editor.putString("user_password", newPassword)
        editor.apply()
        
        // Clear password fields
        etCurrentPassword.setText("")
        etNewPassword.setText("")
        etConfirmNewPassword.setText("")
        
        Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_LONG).show()
    }
}
