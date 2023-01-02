package com.project.consonant.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.consonant.domain.Category;
import com.project.consonant.domain.CreateGameCommand;
import com.project.consonant.domain.Game;
import com.project.consonant.domain.InputQuiz;
import com.project.consonant.domain.Member;
import com.project.consonant.service.GameService;
import com.project.consonant.service.MemberService;
import com.project.consonant.service.exception.GameException;

@Controller
@RequestMapping("/game")
public class GameController {
	@Autowired
	GameService gameSvc;
	@Autowired
	MemberService memberSvc;
	
	//게임 생성
	/*게임 만드는 화면으로 이동*/
	@GetMapping("/createGame")
	public String goCreateGame(Model model, HttpSession session) throws Exception{
		Member memberInfo = (Member) session.getAttribute("member");
		System.out.println(memberInfo.getMemberId());
		List<Category> categoryList = gameSvc.goCreateGame();
		//ModelAndView mav = new ModelAndView();
		model.addAttribute("memberInfo", memberInfo);
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("createGameCommand", new CreateGameCommand());
		model.addAttribute("inputQuiz", new InputQuiz());
		//model.addAttribute("createGame");
		
		return "createGame";
	}
	
	//게임 생성-등록
	@PostMapping("/createGame")
	public ModelAndView createGame(HttpSession session, @Valid @ModelAttribute("createGameCommand") CreateGameCommand createGameCommand, BindingResult result, @ModelAttribute("inputQuiz") InputQuiz inputQuiz) throws Exception{
		
		Member memberInfo = (Member) session.getAttribute("member");
		ModelAndView mav = new ModelAndView();
		if (result.hasErrors()) {
			List<Category> categoryList = gameSvc.getAllCategory();
			List<InputQuiz> inputQuizList = gameSvc.getInputQuizList();
			mav.addObject("categoryList", categoryList);
			mav.addObject("inputQuizList", inputQuizList);
			mav.setViewName("createGame");
		
			return mav;
		}
		
		//System.out.println(createGameCommand.getGameTitle() + " " + createGameCommand.getGameDifficulty());
			
		try {
			List<InputQuiz> inputQuizList = gameSvc.getInputQuizList();
			createGameCommand.setMemberId(memberInfo.getMemberId());
			//System.out.println("아이디: " + createGameCommand.getMemberId() + " 퀴즈리스트 사이즈: "+inputQuizList.size());
			boolean createResult = gameSvc.createGame(createGameCommand, inputQuizList);
		
			if(createResult == true) {
				memberSvc.updatePoint(memberInfo.getMemberId(), 50, 1);
				Member newMemberInfo = memberSvc.findMember(memberInfo.getMemberId());
				newMemberInfo.setPasswd(null);
				session.setAttribute("member", newMemberInfo);
			}
		 // 리스트로 가도록 수정
			mav.setViewName("redirect:/game/gameList");
		}catch (GameException e) {
			List<Category> categoryList = gameSvc.getAllCategory();
			List<InputQuiz> inputQuizList = gameSvc.getInputQuizList();
			mav.addObject("categoryList", categoryList);
			mav.addObject("inputQuizList", inputQuizList);
			mav.addObject("createFailed", true);
			mav.addObject("e", e.getMessage());
			mav.setViewName("redirect:/game/createGame");
			return mav;
		}	
		return mav;
	}
	
	//게임 생성할때 퀴즈 추가
	@PostMapping("/insertQuiz")
	public String insertQuiz(Model model, @Valid @RequestBody InputQuiz inputQuiz, BindingResult result, @ModelAttribute("createGameCommand") CreateGameCommand createGameCommand) throws Exception{

		
		if (result.hasErrors()) {
			return "createGame::#insertQuizForm";
		}
		
		List<InputQuiz> inputQuizList = gameSvc.insertQuiz(inputQuiz);
		model.addAttribute("inputQuizList", inputQuizList);
		return "createGame::#quizBox";
	}
	
	//게임 생성할때 추가했던 퀴즈 삭제
	@PostMapping("/removeQuiz")
	public String removeQuiz(Model model, @RequestBody String question) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = mapper.readValue(question, Map.class);
		List<InputQuiz> inputQuizList = gameSvc.removeQuiz(map.get("question"));
		model.addAttribute("inputQuizList", inputQuizList);
		return "createGame::#resultDiv";
	}
	
	
	
	
	
	//게임 리스트
	@GetMapping("/gameList")
	public String goGameList(Model model, HttpSession session) throws Exception{
		Member memberInfo = (Member) session.getAttribute("member");
		System.out.println(memberInfo.getMemberId());
		List<Category> categoryList = gameSvc.getAllCategory();
		List<Game> gameList = gameSvc.findAllGames(memberInfo.getMemberId());
	
		model.addAttribute("memberInfo", memberInfo);
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("gameList", gameList);
		
		return "gameList";
	}
	
	//카테고리별 게임 리스트
	@GetMapping("/category/gameList")
	public String gameListByCategory(Model model, HttpSession session, String categoryId) throws Exception{
		Member memberInfo = (Member) session.getAttribute("member");
		System.out.println(memberInfo.getMemberId());
		List<Category> categoryList = gameSvc.getAllCategory();
		List<Game> gameList = gameSvc.findAllGamesByCategory(memberInfo.getMemberId(), categoryId);
		
		model.addAttribute("memberInfo", memberInfo);
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("gameList", gameList);
			
		return "gameList::#gameListDiv";
	}
	
	//게임 시작
	@GetMapping("/playGame/{gameNo}")
	public String playGame(Model model, HttpSession session, @PathVariable("gameNo") int gameNo) throws Exception{
		Member memberInfo = (Member) session.getAttribute("member");
		model.addAttribute("gameNo", gameNo);
		return "playGame";
	}
}
