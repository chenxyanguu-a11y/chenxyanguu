package com.example.malluser.service.impl;

import com.example.malluser.dto.UserInfoResponse;
import com.example.malluser.exception.UserException;
import com.example.malluser.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void listNonAdminUsersReturnsUsersForAdmin() {
        Long currentUserId = 1L;
        UserInfoResponse user = new UserInfoResponse();
        user.setId(2L);
        user.setUsername("merchant");
        when(userMapper.selectNonAdminUsers(currentUserId)).thenReturn(List.of(user));

        List<UserInfoResponse> users = adminUserService.listNonAdminUsers(currentUserId, "ADMIN");

        assertEquals(1, users.size());
        assertEquals(2L, users.get(0).getId());
        assertEquals("merchant", users.get(0).getUsername());
        verify(userMapper).selectNonAdminUsers(currentUserId);
    }

    @Test
    void listNonAdminUsersRejectsNonAdminRole() {
        UserException exception = assertThrows(UserException.class,
                () -> adminUserService.listNonAdminUsers(1L, "MERCHANT"));

        assertEquals(403, exception.getCode());
    }
}
