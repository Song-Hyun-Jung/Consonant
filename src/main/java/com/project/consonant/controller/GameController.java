package com.project.consonant.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForCharSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.consonant.domain.Category;
import com.project.consonant.domain.CreateGameCommand;
import com.project.consonant.domain.InputQuiz;
import com.project.consonant.domain.Member;
import com.project.consonant.service.GameService;
import com.project.consonant.service.exception.GameException;
import com.project.consonant.service.exception.LoginException;

@Controller
@RequestMapping("/game")
public class GameController {
	@Autowired
	GameService gameSvc;
	
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
		model.addAttribute("createGame");
		
		return "createGame";
	}
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
			System.out.println("아이디: " + createGameCommand.getMemberId() + " 퀴즈리스트 사이즈: "+inputQuizList.size());
			boolean createResult = gameSvc.createGame(createGameCommand, inputQuizList);
		 // 리스트로 가도록 수정
			mav.setViewName("home");
		}catch (GameException e) {
			List<Category> categoryList = gameSvc.getAllCategory();
			List<InputQuiz> inputQuizList = gameSvc.getInputQuizList();
			mav.addObject("categoryList", categoryList);
			mav.addObject("inputQuizList", inputQuizList);
			mav.addObject("createFailed", true);
			mav.addObject("e", e.getMessage());
			mav.setViewName("createGame");
			return mav;
		}	
		return mav;
	}
	@PostMapping("/insertQuiz")
	public String insertQuiz(Model model, @Valid @RequestBody InputQuiz inputQuiz, BindingResult result, @ModelAttribute("createGameCommand") CreateGameCommand createGameCommand) throws Exception{

		
		if (result.hasErrors()) {
			return "createGame::#insertQuizForm";
		}
		
		List<InputQuiz> inputQuizList = gameSvc.insertQuiz(inputQuiz);
		model.addAttribute("inputQuizList", inputQuizList);
		return "createGame::#quizBox";
	}
	
	@PostMapping("/removeQuiz")
	public String removeQuiz(Model model, @RequestBody String question) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = mapper.readValue(question, Map.class);
		List<InputQuiz> inputQuizList = gameSvc.removeQuiz(map.get("question"));
		model.addAttribute("inputQuizList", inputQuizList);
		return "createGame::#resultDiv";
	}
}