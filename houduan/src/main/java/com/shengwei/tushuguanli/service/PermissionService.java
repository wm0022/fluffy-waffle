package com.shengwei.tushuguanli.service;

import java.util.List;

public interface PermissionService {
    List<String> getAdminMenuPaths(Long userId);
}

