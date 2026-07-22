package com.checkpoint.modules.lists.dto;

import java.util.List;
import java.util.UUID;

public record ListMembershipResponse(List<UUID> listIds) {}