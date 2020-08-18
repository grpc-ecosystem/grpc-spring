package my.suveng.istiocloudorderdemo.controller;

import my.suveng.istiocloudorderdemo.consumer.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author suwenguang
 **/
@RestController
public class Controller {

	@Autowired
	Service service;

	@RequestMapping("/pay")
	public String pay(){
		service.t1();
		return "success";
	}
}
