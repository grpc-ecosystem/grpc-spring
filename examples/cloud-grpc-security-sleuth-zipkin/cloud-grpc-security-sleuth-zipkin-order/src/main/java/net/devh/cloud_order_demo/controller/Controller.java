package net.devh.cloud_order_demo.controller;

import lombok.extern.slf4j.Slf4j;
import net.devh.cloud_order_demo.consumer.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试入口逻辑
 * @author suwenguang
 **/
@RestController
@Slf4j
public class Controller {

	/**
	 * 测试方法
	 */
	@Autowired
    Service service;

	@RequestMapping("/pay")
	public String pay(){
		service.asyncPayNoResult();
		return "please check console log and debug";
	}
}
