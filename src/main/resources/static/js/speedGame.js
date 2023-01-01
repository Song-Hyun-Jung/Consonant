let setTime = 60;	// 최초 설정 시간(기본 : 초)
let answerList = [];
const regex = /^[가-힣|]+$/;

window.onload = function() { 
	// 게임 타이머 설정
	tid = setInterval('msgTime()', 1000);
	
	// 엔터로 버튼 이벤트가 활성화되도록 설정
	const input = document.getElementById("answer");
	input.addEventListener("keyup", function (event) {
	if (event.keyCode === 13) {
        event.preventDefault();
        document.getElementById("answer_btn").click();
      }
	});
	
	// 초성이 2개인 경우 td에 colspan 속성 추가
	if (consonants.length == 2) {
		let tdList = document.querySelectorAll(".game_table td");
		tdList.forEach(function(td) {
			if (td.getAttribute("class") == "question") {
				return;
			}
			td.colSpan = 2;
		});
	}
};

function msgTime() {
	let msg = "제한시간<br/>" + setTime;
	
	let div = document.getElementById("timer");
	div.innerHTML = msg;

	setTime--;
			
	if (setTime < 0) {
		clearInterval(tid);
		alert("게임 종료");
		
		sendAnswer();
	}
			
}

function checkAnswer() {
	const input = document.getElementById("answer");
	const answer = (input.value).trim();
	
	// 비어있는지 확인
	if (answer == "") {
		alert("공백은 입력할 수 없습니다.");
		input.value = "";
		return;
	}
	// 한글인지 확인
	if (!regex.test(answer)) {
		alert("한글만 입력할 수 있습니다.");
		input.value = "";
		return;
	}
	// 글자 수 확인
	if (answer.length != consonants.length) {
		alert("글자 수가 맞지 않습니다.");
		input.value = "";
		return;
	}

	input.value = "";
	addAnswer(answer);
}

function addAnswer(answer) {
	answerList.push(answer);
	
	let viewText = "";
	answerList.forEach(function(answer, index) {
		viewText += answer + " ";
		if ((index + 1) % 5 == 0) {
			viewText += "<br/>";
		}
	});
	
	let viewAnswer = document.getElementById("viewAnswer");
	viewAnswer.innerHTML = viewText;
}

function sendAnswer() {
	let form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", "/game/speed/resultGame");

    let hiddenField = document.createElement("input");
    hiddenField.setAttribute("type",  "hidden");
    hiddenField.setAttribute("name", "consonants");
    hiddenField.setAttribute("value", consonants);
    form.appendChild(hiddenField);
    
    hiddenField = document.createElement("input");
    hiddenField.setAttribute("type",  "hidden");
    hiddenField.setAttribute("name", "answers");
    hiddenField.setAttribute("value", answerList);
    form.appendChild(hiddenField);

    document.body.appendChild(form);
    form.submit();
}