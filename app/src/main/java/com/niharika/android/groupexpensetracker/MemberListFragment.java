package com.niharika.android.groupexpensetracker;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private static final int REQUEST_CONTACT = 0, noMembers = 0;
    private RecyclerView mMemberListRecyclerView;
    private MemberAdapter mAdapter;
    private int positionClicked = -1;
    private Account mAccount;
    private List<Member> mMemberList;
    private static final String ARG_ACC_ID = "account_id", ARG_MEMBER_ID = "member_id";
    private String accNo, mAdminId;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_account_list, menu);
        setFragmentTitle();
        ((MainActivity) getActivity()).showDrawer(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setFragmentTitle() {
        getActivity().setTitle(mAccount.getAccName());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ACC_ID)) {
            accNo = (String) getArguments().getSerializable(ARG_ACC_ID);
            if (accNo != null)
                mAccount = AccountLab.get(getActivity()).getAccount(accNo);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_list, container, false);
        mMemberListRecyclerView = (RecyclerView) view.findViewById(R.id.member_list_recycler_view);
        mMemberListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMemberList = new ArrayList<Member>();
        updateMemberUI();
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public void updateMemberUI() {
        mAccount.getMemberIds(new Account.FirebaseCallbackMemberIds() {
            @Override
            public void onCallbackMemberIds(final List<String> account_memberList, String adminId) {
                if (account_memberList.size() > noMembers) {
                    mAdminId = adminId;
                    mAccount.getMembers(new Account.FirebaseCallbackMember() {
                        public void onCallback(ArrayList<Member> memberList) {
                            if (memberList.size() > noMembers) {
                                if (mAdapter == null) {
                                    mAdapter = new MemberAdapter(new ArrayList<Member>(memberList));
                                    mMemberListRecyclerView.setAdapter(mAdapter);
                                } else {
                                    mAdapter.setMembers(new ArrayList<Member>(memberList));
                                    mMemberListRecyclerView.setAdapter(mAdapter);
                                }
                            }

                        }
                    }, account_memberList);
                } else {
                    mMemberListRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    private class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mMemberName, mMemberRole;
        private ImageButton mDelMemberButton;
        private Member mMember;

        public MemberHolder(@NonNull View itemView) {
            super(itemView);
            mMemberName = (TextView) itemView.findViewById(R.id.member_name);
            mDelMemberButton = (ImageButton) itemView.findViewById(R.id.member_del_button);
            mMemberRole = (TextView) itemView.findViewById(R.id.member_role);
            itemView.setOnClickListener(this);
            mDelMemberButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            positionClicked = getAdapterPosition();
            if (view.getId() == R.id.member_del_button) {
                mAccount.delMember(mMember.getMemberId());
                updateMemberUI();
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_ACC_ID, mAccount.getAccNo());
                bundle.putSerializable(ARG_MEMBER_ID, mMember.getMemberId());
                Navigation.findNavController(view).navigate(R.id.action_memberListFragment_to_editMemberFragment, bundle);
            }
        }

        public void bindMember(Member member) {
            mMember = member;
            if (mMember.getMemberName() != null)
                mMemberName.setText(mMember.getMemberName());
            else if (mMember.getEmailId() != null)
                mMemberName.setText(mMember.getEmailId());
            else
                mMemberName.setText(mMember.getMobNo());
            if (mAdminId != null)
                if (mAdminId.equals(mMember.getMemberId())) {
                    mMemberRole.setVisibility(View.VISIBLE);
                    mMemberRole.setText(R.string.admin_label);
                    mDelMemberButton.setVisibility(View.INVISIBLE);
                }
        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberHolder> {
        private List<Member> memberList;

        public MemberAdapter(List<Member> members) {
            memberList = new ArrayList<Member>();
            memberList = members;
        }

        public void setMembers(List<Member> members) {
            memberList.clear();
            memberList = members;
        }

        @NonNull
        @Override
        public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_member_add_account, parent, false);
            return new MemberHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberHolder holder, int position) {
            Member member = memberList.get(position);
            holder.bindMember(member);
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String mName = c.getString(0);
                c.close();
                final Member member = new Member(AccountLab.get(getActivity()).getNewMemberId());
                member.setMemberName(mName);
                queryFields = new String[]{ContactsContract.Contacts._ID};
                c = getActivity().getContentResolver()
                        .query(contactUri, queryFields, null, null, null);
                String contactID = null;
                if (c.moveToFirst()) {
                    contactID = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                }
                c.close();
                queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                c = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, queryFields, selection, new String[]{contactID}, null);
                String contactNumber = null;
                if (c.moveToFirst()) {
                    contactNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    member.setMobNo(contactNumber);
                }
                c.close();
                String email = null;
                selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
                c = getActivity().getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, selection, new String[]{contactID}, null);
                if (c != null && c.moveToFirst()) {
                    email = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    member.setEmailId(email);
                }
                final Member newMember = member;
                final String finalEmail = email;
                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                    @Override
                    public void onCallback(Member oldMember) {
                        if (oldMember == null) {
                            if (finalEmail == null) {
                                AccountLab.get(getActivity()).addMember(newMember);
                                mAccount.addMemberToAccount(newMember.getMemberId(), "MEMBER");
                                updateMemberUI();
                            } else
                                AccountLab.get(getActivity()).checkUserExist(new AccountLab.FirebaseCallback() {
                                    @Override
                                    public void onCallback(Member oldMember) {
                                        if (oldMember == null) {
                                            AccountLab.get(getActivity()).addMember(newMember);
                                            mAccount.addMemberToAccount(newMember.getMemberId(), "MEMBER");
                                            updateMemberUI();

                                        } else {
                                            newMember.setMemberId(oldMember.getMemberId());
                                            AccountLab.get(getActivity()).updateMember(newMember);
                                            mAccount.addMemberToAccount(newMember.getMemberId(), "MEMBER");
                                            updateMemberUI();
                                        }
                                    }
                                }, finalEmail, "E");
                        } else {
                            newMember.setMemberId(oldMember.getMemberId());
                            AccountLab.get(getActivity()).updateMember(newMember);
                            mAccount.addMemberToAccount(newMember.getMemberId(), "MEMBER");
                            updateMemberUI();
                        }
                    }
                }, contactNumber, "P");
            } finally {
                c.close();
            }
        }
    }
}
