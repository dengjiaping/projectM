package com.yywl.projectT.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.DistanceConverter;
import com.yywl.projectT.bean.MD5Util;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.bean.ValidatorBean;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.ActivityStates;
import com.yywl.projectT.bean.enums.OctRoomEnum;
import com.yywl.projectT.bo.AdminBo;
import com.yywl.projectT.bo.OctRoomBo;
import com.yywl.projectT.bo.RoomBo;
import com.yywl.projectT.dao.AdminDao;
import com.yywl.projectT.dao.ApplicationDao;
import com.yywl.projectT.dao.ComplaintDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.LocationDao;
import com.yywl.projectT.dao.NotLateReasonDao;
import com.yywl.projectT.dao.OctRoomDao;
import com.yywl.projectT.dao.OctRoomUserDao;
import com.yywl.projectT.dao.PropTypeDao;
import com.yywl.projectT.dao.RoomDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.SpreadUserDao;
import com.yywl.projectT.dao.SuggestionDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dao.WithdrawalsDao;
import com.yywl.projectT.dmo.AdminDmo;
import com.yywl.projectT.dmo.ApplicationDmo;
import com.yywl.projectT.dmo.ComplaintDmo;
import com.yywl.projectT.dmo.LocationDmo;
import com.yywl.projectT.dmo.NotLateReasonDmo;
import com.yywl.projectT.dmo.OctRoomDmo;
import com.yywl.projectT.dmo.PropTypeDmo;
import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.SpreadUserDmo;
import com.yywl.projectT.dmo.SuggestionDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.dmo.WithdrawalsDmo;

@RequestMapping("admin")
@RestController
public class AdminController {

	@Autowired
	AdminBo adminBo;
	@Autowired
	AdminDao adminDao;

	@Autowired
	LocationDao locationDao;

	@Autowired
	RoomDao roomDao;

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Autowired
	UserDao userDao;

	@Autowired
	RoomMemberDao roomMemberDao;

	@Autowired
	ComplaintDao complaintDao;

	@Autowired
	PropTypeDao propTypeDao;

	@Autowired
	SuggestionDao suggestionDao;

	@Autowired
	NotLateReasonDao noteLateReasonDao;

	@Autowired
	OctRoomDao octRoomDao;

	@Autowired
	OctRoomBo octRoomBo;

	@Autowired
	OctRoomUserDao octRoomUserDao;
	
	@Autowired
	RongCloudBean rongCloud;
	
	@PostMapping("octRefuse")
	public Callable<ResultModel> octRefuse(long loginId, String token,long roomId,String reason){
		return ()->{
			this.adminBo.loginByToken(loginId, token);
			OctRoomDmo dmo=this.octRoomDao.findByRoomId(roomId);
			if (dmo == null) {
				throw new Exception("用户不在房间内");
			}
			if (dmo.getState()!=OctRoomEnum.审核中.ordinal()) {
				throw new Exception("请勿重复操作");
			}
			this.octRoomBo.refuce(dmo,loginId,reason);
			return new ResultModel();
		};
	}
	
	@PostMapping("setFoul")
	public Callable<ResultModel> setFoul(long loginId, String token,long userId,long roomId,boolean foul){
		return ()->{
			this.adminBo.loginByToken(loginId, token);
			this.octRoomBo.setFoul(userId,foul);
			return new ResultModel();
		};
	}
	
	@PostMapping("octReward")
	public Callable<ResultModel> octReward(long loginId, String token, long roomId) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			OctRoomDmo octRoomDmo = this.octRoomDao.findByRoomId(roomId);
			if (octRoomDmo == null) {
				throw new Exception("用户不在房间内");
			}
			if (octRoomDmo.getState()!=OctRoomEnum.审核中.ordinal()) {
				throw new Exception("请勿重复操作");
			}
			this.octRoomBo.reward(octRoomDmo, loginId);
			return new ResultModel();
		};
	}

	@PostMapping("findOctRoomUsers")
	public Callable<ResultModel> findOctRoomUsers(long loginId, String token, long roomId) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			List<Map<String, Object>> list = jdbcDao.findOctRoomUsers(roomId);
			return new ResultModel(true, null, list);
		};
	}

	@PostMapping("findOctRooms")
	public Callable<ResultModel> findOctRooms(long loginId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			List<Map<String, Object>> list = this.jdbcDao.findOctRooms(ValidatorBean.page(page),
					ValidatorBean.size(size));
			Map<String, Object> data = new HashMap<>();
			data.put("content", list);
			int count = this.jdbcDao.countOctRooms();
			int totalPages = count % size == 0 ? count / size : (count / size) + 1;
			data.put("totalPages", totalPages);
			return new ResultModel(true, null, data);
		};
	}

	@PostMapping("spreadUser/add")
	public Callable<ResultModel> addSpreadUser(long loginId, String token, long userId, double longitude1,
			double longitude2, double latitude1, double latitude2) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			this.adminBo.addSpreadUser(userId, longitude1, longitude2, latitude1, latitude2);
			return new ResultModel();
		};
	}

	@PostMapping("spreadUser/update")
	public Callable<ResultModel> updateSpreadUser(long loginId, String token, long id, long userId, double longitude1,
			double longitude2, double latitude1, double latitude2) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			this.adminBo.updateSpreadUser(id, userId, longitude1, longitude2, latitude1, latitude2);
			return new ResultModel();
		};
	}

	/**
	 * 管理员设置所有人状态为准备
	 */
	@PostMapping("room/readyMember")
	public Callable<ResultModel> readyMember(long userId, String token, long roomId) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			int fail = this.roomBo.readyMember(roomId);
			if (fail > 0) {
				return new ResultModel(false, fail + "人观影券不足", null);
			}
			return new ResultModel();
		};
	}

	@PostMapping("spreadUser/remove")
	public Callable<ResultModel> removeSpreadUser(long loginId, String token, long id) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			this.spreadUserDao.delete(id);
			return new ResultModel();
		};
	}

	@PostMapping("findPropTypes")
	public Callable<ResultModel> findPropTypes(long loginId, String token) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			List<PropTypeDmo> list = this.propTypeDao.findAll(new Sort(Direction.ASC, "uniqueId"));
			return new ResultModel(true, "", list);
		};
	}

	@PostMapping("photoType/remove")
	public Object removePhotoType(long loginId, String token, int id) throws Exception {
		this.adminBo.loginByToken(loginId, token);
		this.propTypeDao.delete(id);
		return new ResultModel();
	}

	@PostMapping("addPhotoType")
	public Object addPhotoType(long loginId, String token, String name, String title, String description, int money,
			int badge, String photoUrl, int uniqueId) throws Exception {
		this.adminBo.loginByToken(loginId, token);
		PropTypeDmo dmo = new PropTypeDmo(null, uniqueId, name, title, photoUrl, description, badge, money);
		this.propTypeDao.save(dmo);
		return new ResultModel();
	}

	@PostMapping("updatePhotoType")
	public Object updatePhotoType(long loginId, int id, String token, String name, String title, String description,
			int money, int badge, String photoUrl, int uniqueId) throws Exception {
		this.adminBo.loginByToken(loginId, token);
		if (!this.propTypeDao.exists(id)) {
			return new ResultModel(false, "id不存在", null);
		}
		PropTypeDmo dmo = new PropTypeDmo(id, uniqueId, name, title, photoUrl, description, badge, money);
		this.propTypeDao.save(dmo);
		return new ResultModel();
	}

	@PostMapping("findReason")
	public Callable<ResultModel> findReason(long loginId, String token, long notLateId) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			NotLateReasonDmo dmo = this.noteLateReasonDao.findOne(notLateId);
			if (null == dmo) {
				return new ResultModel(false, "信息不存在", null);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("userId", dmo.getUser().getId());
			map.put("nickname", dmo.getUser().getNickname());
			map.put("certifierId", dmo.getCertifierId());
			map.put("userPhone", dmo.getUser().getPhone());
			map.put("userRealName", dmo.getUser().getRealName());
			UserDmo certifier = this.userDao.findOne(dmo.getCertifierId());
			map.put("certifier", certifier);
			map.put("room", dmo.getRoom());
			map.put("reason", dmo.getReason());
			map.put("createTime", dmo.getCreateTime() != null ? dateFormat.format(dmo.getCreateTime()) : "");
			map.put("photoUrl", dmo.getPhotoUrl());
			map.put("dealState", dmo.getDealState());
			return new ResultModel(true, "", map);
		};
	}

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@PostMapping("findComplaint")
	public Callable<ResultModel> findComplaint(long loginId, String token, long complaintId) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			return new ResultModel(true, "", this.complaintDao.findOne(complaintId));
		};
	}

	@PostMapping("fenFa")
	public Callable<ResultModel> fenFa(long id, long userId, String token) {
		return () -> {
			AdminDmo admin = this.adminBo.loginByToken(userId, token);
			this.adminBo.fenFa(id, admin);
			return new ResultModel();
		};
	}

	/**
	 * 查看申请未迟到的用户
	 */
	@PostMapping("findAttend")
	public Callable<ResultModel> findAttend(long userId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			Pageable pageable = new PageRequest(page, size, Direction.DESC, "createTime");
			Page<NotLateReasonDmo> reasons = this.noteLateReasonDao.findAll(pageable);
			return new ResultModel(true, null, reasons);
		};
	}

	@PostMapping("findComplaints")
	public Callable<ResultModel> findComplaints(long userId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			Page<ComplaintDmo> complaint = this.complaintDao
					.findAll(new PageRequest(page, size, Direction.DESC, "createTime"));
			return new ResultModel(true, null, complaint);
		};
	}

	@PostMapping("findLocationByRoom_Id")
	public Callable<ResultModel> findLocationByRoom_Id(long loginId, String token, long userId, long roomId) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			List<Map<String, Object>> list = new LinkedList<>();
			Order order = new Sort.Order(Direction.DESC, "sendTime");
			Sort sort = new Sort(order);
			List<LocationDmo> locations = this.locationDao.findByUser_IdAndRoom_Id(userId, roomId, sort);
			for (LocationDmo l : locations) {
				double distance = DistanceConverter.getDistance(l.getLongitude(), l.getLatitude(),
						l.getRoom().getLongitude(), l.getRoom().getLatitude());
				Map<String, Object> map = new HashMap<>();
				map.put("ip", l.getIp());
				map.put("latitude", l.getLatitude());
				map.put("longitude", l.getLongitude());
				map.put("sendTime", dateFormat.format(l.getSendTime()));
				map.put("distance", String.format("%.2f", distance));
				map.put("place", l.getPlace());
				list.add(map);
			}
			return new ResultModel(true, null, list);
		};
	}

	@PostMapping("findLocationAroundBeginTime")
	public Callable<ResultModel> findLocationAroundBeginTime(long loginId, String token, long userId, long roomId) {

		return () -> {
			this.adminBo.loginByToken(loginId, token);
			RoomDmo room = this.roomDao.findOne(roomId);
			List<Map<String, Object>> list = this.jdbcDao.findLocationAroundBeginTime(userId, room);
			for (Map<String, Object> map : list) {
				if (map.get("distance") == null) {
					continue;
				}
				double distance = (Double) map.get("distance");
				map.put("distance", String.format("%.2f", distance));
			}
			return new ResultModel(true, null, list);
		};
	}

	@Autowired
	JdbcDao jdbcDao;

	@PostMapping("findSuggestions")
	public Callable<ResultModel> findSuggestions(long userId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			Page<SuggestionDmo> suggestions = this.suggestionDao
					.findAll(new PageRequest(page, size, Direction.DESC, "createTime"));
			return new ResultModel(true, null, suggestions);
		};
	}

	@PostMapping("findUserInfo")
	public Callable<ResultModel> findUserInfo(long userId, String token, Long id) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			if (id == null) {
				log.error("用户不存在");
				throw new Exception("用户不存在");
			}
			return new ResultModel(true, null, this.userDao.findOne(id));
		};
	}

	/**
	 * 解冻保证金
	 */
	@PostMapping("jieDong")
	@Transactional(rollbackOn = Throwable.class)
	public Callable<ResultModel> jieDong(long userId, String token, long id) {
		return () -> {
			AdminDmo admin = this.adminBo.loginByToken(userId, token);
			this.adminBo.jieDong(id, admin);
			return new ResultModel();
		};
	}

	@PostMapping("login")
	public Callable<ResultModel> login(String username, String password) {
		return () -> {
			if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
				log.error("用户名或密码不能为空");
				throw new Exception("用户名或密码不能为空");
			}
			AdminDmo admin = this.adminDao.findByUsername(username);
			if (admin == null) {
				log.info("用户名不存在");
				throw new Exception("用户名不存在或密码不正确");
			}
			if (!admin.getPassword().equals(MD5Util.getSecurityCode(password))) {
				log.error("密码不正确");
				throw new Exception("密码不正确或密码不正确");
			}
			admin.setToken(UUID.randomUUID().toString());
			admin.setExpire(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
			this.adminDao.save(admin);
			return new ResultModel(true, "", admin);
		};
	}

	private final static Log log = LogFactory.getLog(AdminController.class);

	@PostMapping("findWithdrawals")
	public Callable<ResultModel> findWithdrawals(long userId, String token, Integer state, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC,
					"createTime");
			Page<WithdrawalsDmo> withdrawalsDmoPage;
			if (null == state) {
				withdrawalsDmoPage = this.withdrawalsDao.findAll(pageable);
			} else {
				withdrawalsDmoPage = this.withdrawalsDao.findByState(state, pageable);
			}
			return new ResultModel(true, "", withdrawalsDmoPage);
		};
	}

	@Autowired
	ApplicationDao applicationDao;

	@PostMapping("findApplications")
	public Callable<ResultModel> findApplications(long userId, String token) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			List<ApplicationDmo> list = this.applicationDao.findAll();
			return new ResultModel(true, "", list);
		};
	}

	@PostMapping("changeVersion")
	public Callable<ResultModel> changeVersion(long userId, String token, int id, String version) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			if (StringUtils.isEmpty(version)) {
				log.error("version不能为空");
				return new ResultModel(false, "version不能为空", null);
			}
			ApplicationDmo applicationDmo = this.applicationDao.findOne(id);
			applicationDmo.setVersion(version);
			this.applicationDao.save(applicationDmo);
			return new ResultModel(true, "修改成功", applicationDmo);
		};
	}

	@PostMapping("changeVersionV2")
	public Callable<ResultModel> changeVersionV2(long userId, String token, int id, String version, String downUrl,
			String message, String force, String current, boolean remind) {
		return () -> {
			this.adminBo.loginByToken(userId, token);
			if (StringUtils.isEmpty(version)) {
				log.error("version不能为空");
				return new ResultModel(false, "version不能为空", null);
			}
			ApplicationDmo applicationDmo = this.applicationDao.findOne(id);
			applicationDmo.setIsCurrent("true".equalsIgnoreCase(current));
			applicationDmo.setDownUrl(downUrl);
			applicationDmo.setMessage(message);
			applicationDmo.setForce("true".equalsIgnoreCase(force));
			applicationDmo.setVersion(version);
			applicationDmo.setRemind(remind);
			this.applicationDao.save(applicationDmo);
			return new ResultModel(true, "修改成功", applicationDmo);
		};
	}

	@PostMapping("findSuggestion")
	public Callable<ResultModel> findSuggestion(long suggestionId, long loginId, String token) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			SuggestionDmo dmo = this.suggestionDao.findOne(suggestionId);
			return new ResultModel(true, "", dmo);
		};
	}

	@Autowired
	RoomBo roomBo;

	@PostMapping("deleteRoom")
	public Callable<ResultModel> deleteRoom(long loginId, String token, long roomId, String reason) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			RoomDmo room = this.roomDao.findOne(roomId);
			if (null == room) {
				throw new Exception("房间id不存在");
			}
			if (room.getState() > 1) {
				throw new Exception("房间已开始");
			}
			this.roomBo.delete(room, reason);
			return new ResultModel();
		};
	}

	@PostMapping("findRooms")
	public Callable<ResultModel> findRooms(long loginId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			Pageable pageable = new PageRequest(ValidatorBean.page(page), ValidatorBean.size(size), Direction.DESC,
					"beginTime");
			Page<RoomDmo> rooms = this.roomDao.findByStateAndPrepareTimeIsNull(ActivityStates.新建.ordinal(), pageable);
			return new ResultModel(true, "", rooms);
		};
	}

	@Autowired
	SpreadUserDao spreadUserDao;

	@PostMapping("findSpreadUsers")
	public Callable<ResultModel> findSpreadUsers(long loginId, String token, int page, int size) {
		return () -> {
			this.adminBo.loginByToken(loginId, token);
			Map<String, Object> map = new HashMap<>();
			long totalElements = this.spreadUserDao.count();
			double totalPages = Math.ceil(totalElements * 1.0 / size);
			List<SpreadUserDmo> content = this.jdbcDao.findSpreadUsers(ValidatorBean.page(page),
					ValidatorBean.size(size));
			map.put("totalPages", totalPages);
			map.put("content", content);
			return new ResultModel(true, "", map);
		};
	}

	@Autowired
	WithdrawalsDao withdrawalsDao;
}
