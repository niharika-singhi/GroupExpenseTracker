package com.niharika.android.groupexpensetracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@RunWith(JUnit4.class)
public class AccountJUnitTest {
    private Account mAccount;

    @Before
    public void setUp(){
       mAccount=new Account();
    }
    @Test
    public void accountShouldNotBeDefaultAccountUntilSet() {
        boolean resultAccount=mAccount.isDefaultAccount();

        assertThat(resultAccount,is(equalTo(false)));
    }
}
