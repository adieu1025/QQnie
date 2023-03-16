package com.canon.QQnie.dto;

import com.canon.QQnie.entity.OrderDetail;
import com.canon.QQnie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
