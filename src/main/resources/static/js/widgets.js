// widgets.js - 홈 화면 스마트폰 스타일 위젯 제어 스크립트

document.addEventListener("DOMContentLoaded", () => {
    // 1. 위젯 데이터 초기화 호출
    initCountdownWidget();
    fetchWeatherWidgetData();
    fetchTransitWidgetData();
    fetchBalanceGameWidgetData();

    // 2. 관리자 폼 이벤트 바인딩
    const countdownForm = document.getElementById("countdown-config-form");
    if (countdownForm) {
        countdownForm.addEventListener("submit", handleCountdownConfigSubmit);
    }

    const balanceForm = document.getElementById("balance-create-form");
    if (balanceForm) {
        balanceForm.addEventListener("submit", handleBalanceCreateSubmit);
    }
});

// 전역 변수
let countdownInterval = null;
let activeQuestionId = null;

// ==========================================
// 1. 퇴근 카운트다운 위젯
// ==========================================
function initCountdownWidget() {
    fetch("/api/widgets/countdown")
        .then(res => {
            if (res.status === 204) return null;
            return res.json();
        })
        .then(data => {
            if (!data) return;

            // 남은 근무일 및 D-day 세팅
            document.getElementById("countdown-working-days").textContent = data.remainingWorkingDays + "일";
            
            const holidayName = data.nextHolidayName;
            const holidayDday = data.holidayDday;
            const countdownHoliday = document.getElementById("countdown-holiday");
            const holidayLabel = document.getElementById("holiday-label");
            if (holidayDday >= 0) {
                holidayLabel.textContent = holidayName;
                countdownHoliday.textContent = holidayDday === 0 ? "D-Day" : `D-${holidayDday}`;
            } else {
                holidayLabel.textContent = "다음 공휴일";
                countdownHoliday.textContent = "없음";
            }

            const vacationDday = data.vacationDday;
            const countdownVacation = document.getElementById("countdown-vacation");
            const vacationLabel = document.getElementById("vacation-label");
            if (vacationDday >= 0) {
                vacationLabel.textContent = data.dDayLabel;
                countdownVacation.textContent = vacationDday === 0 ? "D-Day" : `D-${vacationDday}`;
            } else {
                countdownVacation.textContent = "- 일";
            }

            // 퇴근 시간 상태 문구 세팅
            const leaveStatus = document.getElementById("leave-status");
            const leaveTimeStr = data.leaveTime.substring(0, 5); // HH:mm
            if (data.isVacation) {
                leaveStatus.textContent = `단축 근무 기간 (${leaveTimeStr} 퇴근)`;
                leaveStatus.className = "text-xs text-emerald-600 font-semibold mt-1";
            } else {
                leaveStatus.textContent = `학기 중 정상 근무 (${leaveTimeStr} 퇴근)`;
                leaveStatus.className = "text-xs text-indigo-600 font-semibold mt-1";
            }

            // 실시간 타이머 시작
            let secondsLeft = data.secondsToLeave;
            const timerDisplay = document.getElementById("leave-timer");

            if (countdownInterval) clearInterval(countdownInterval);

            if (secondsLeft <= 0) {
                timerDisplay.textContent = "퇴근 완료! 🎉";
                timerDisplay.className = "text-3xl md:text-4xl font-black text-emerald-600 tracking-tight animate-bounce";
            } else {
                timerDisplay.className = "text-4xl md:text-5xl font-black text-slate-800 tracking-tight";
                
                function updateTimerDisplay() {
                    if (secondsLeft <= 0) {
                        timerDisplay.textContent = "퇴근 완료! 🎉";
                        timerDisplay.className = "text-3xl md:text-4xl font-black text-emerald-600 tracking-tight animate-bounce";
                        clearInterval(countdownInterval);
                        return;
                    }
                    
                    const h = Math.floor(secondsLeft / 3600);
                    const m = Math.floor((secondsLeft % 3600) / 60);
                    const s = secondsLeft % 60;
                    
                    timerDisplay.textContent = 
                        `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
                    
                    secondsLeft--;
                }

                updateTimerDisplay();
                countdownInterval = setInterval(updateTimerDisplay, 1000);
            }
        })
        .catch(err => console.error("퇴근 카운트다운 로드 오류:", err));
}

// 관리자용 퇴근 설정 데이터 로드
function loadCountdownAdminConfig() {
    fetch("/api/widgets/countdown")
        .then(res => res.json())
        .then(data => {
            const form = document.getElementById("countdown-config-form");
            if (!form) return;

            // D-day 계산을 위한 설정을 백엔드에서 다시 가져오거나 폼에 바인딩
            // 설정 값 목록을 얻기 위해 /api/widgets/countdown API에서 설정 키를 반환하게 하거나 프론트에서 가공
            // 백엔드가 Config 정보 자체를 리스트로 내려주진 않고 매핑해서 사용
            // 폼 데이터를 프리필하기 위해 임시 바인딩 처리
            form.elements["OFFICE_LEAVE_TIME_NORMAL"].value = data.isVacation ? "18:00:00" : data.leaveTime;
            form.elements["OFFICE_LEAVE_TIME_VACATION"].value = data.isVacation ? data.leaveTime : "16:00:00";
            
            // 날짜 데이터가 있으면 바인딩 (없으면 공란)
            // 방학 날짜 등을 백엔드 Config API를 호출해 상세 조회
            fetch("/api/widgets/countdown") // 실무에선 이 API의 설정들을 바로 폼에 대입
                .then(() => {
                    // 예제 포맷을 맞추기 위해 임시 값 설정 (기본값 설정)
                    if (!form.elements["OFFICE_LEAVE_TIME_NORMAL"].value) {
                        form.elements["OFFICE_LEAVE_TIME_NORMAL"].value = "18:00:00";
                    }
                    if (!form.elements["OFFICE_LEAVE_TIME_VACATION"].value) {
                        form.elements["OFFICE_LEAVE_TIME_VACATION"].value = "16:00:00";
                    }
                });
        });
}

// 관리자용 퇴근 설정 변경 전송
function handleCountdownConfigSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const data = {};
    formData.forEach((val, key) => data[key] = val);

    fetch("/api/widgets/countdown/config", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => {
        if (res.ok) {
            alert("퇴근 및 방학 설정이 성공적으로 반영되었습니다.");
            closeAdminCountdownModal();
            initCountdownWidget(); // 위젯 새로고침
        } else {
            res.text().then(text => alert("설정 저장 실패: " + text));
        }
    })
    .catch(err => alert("설정 저장 중 오류가 발생했습니다: " + err));
}


// ==========================================
// 2. 날씨 위젯
// ==========================================
function fetchWeatherWidgetData() {
    fetch("/api/widgets/weather")
        .then(res => res.json())
        .then(data => {
            document.getElementById("weather-current-temp").textContent = data.currentTemp;
            document.getElementById("weather-current-status").textContent = data.currentStatus;
            
            // 날씨 아이콘 변경
            const iconElem = document.getElementById("weather-main-icon");
            iconElem.className = `fa-solid ${data.currentIcon} text-5xl filter drop-shadow-sm`;

            // 시간대별 예보 렌더링
            const hourlyList = document.getElementById("weather-hourly-list");
            hourlyList.innerHTML = ""; // 초기화

            data.forecastHours.forEach(hour => {
                const hourDiv = document.createElement("div");
                hourDiv.className = "flex flex-col items-center flex-shrink-0 text-center min-w-[50px] p-1";
                hourDiv.innerHTML = `
                    <span class="text-[10px] text-slate-400 font-semibold">${hour.time}</span>
                    <i class="fa-solid ${hour.icon} text-lg my-1.5"></i>
                    <span class="text-xs font-bold text-slate-700">${hour.temp}°</span>
                `;
                hourlyList.appendChild(hourDiv);
            });
        })
        .catch(err => {
            console.error("날씨 정보 로드 실패:", err);
            document.getElementById("weather-current-status").textContent = "날씨 정보 에러";
        });
}


// ==========================================
// 3. 대중교통 정보 위젯
// ==========================================
function fetchTransitWidgetData() {
    const subway1 = document.getElementById("transit-subway1-info");
    const subway7 = document.getElementById("transit-subway7-info");
    const busList = document.getElementById("transit-bus-list");

    subway1.textContent = "업데이트 중...";
    subway7.textContent = "업데이트 중...";
    busList.innerHTML = `<span class="text-[10px] text-slate-400 font-bold">로딩 중...</span>`;

    fetch("/api/widgets/transit")
        .then(res => res.json())
        .then(data => {
            // 1호선 매칭
            if (data.subwayLine1 && data.subwayLine1.length >= 2) {
                subway1.innerHTML = `
                    <span class="text-blue-600 font-bold">${data.subwayLine1[0].arrivalTime}</span> (하행) / 
                    <span class="text-blue-600 font-bold">${data.subwayLine1[1].arrivalTime}</span> (상행)
                `;
            } else {
                subway1.textContent = "도착 정보 없음";
            }

            // 7호선 매칭
            if (data.subwayLine7 && data.subwayLine7.length >= 2) {
                subway7.innerHTML = `
                    <span class="text-emerald-600 font-bold">${data.subwayLine7[0].arrivalTime}</span> (하행) / 
                    <span class="text-emerald-600 font-bold">${data.subwayLine7[1].arrivalTime}</span> (상행)
                `;
            } else {
                subway7.textContent = "도착 정보 없음";
            }

            // 버스 정보 매칭
            busList.innerHTML = "";
            if (data.busArrivals && data.busArrivals.length > 0) {
                data.busArrivals.forEach(bus => {
                    const busItem = document.createElement("div");
                    busItem.className = "flex items-center justify-end space-x-1.5 mb-1 last:mb-0";
                    
                    let badgeClass = "bg-slate-200 text-slate-700";
                    if (bus.type === "지선") badgeClass = "bg-green-100 text-green-700";
                    else if (bus.type === "간선") badgeClass = "bg-blue-100 text-blue-700";

                    busItem.innerHTML = `
                        <span class="font-extrabold text-slate-700">${bus.busNo}번</span>
                        <span class="text-[10px] px-1 py-0.2 rounded font-bold ${badgeClass}">${bus.type}</span>
                        <span class="font-black text-rose-500">${bus.arrivalTime}</span>
                    `;
                    busList.appendChild(busItem);
                });
            } else {
                busList.textContent = "도착 정보 없음";
            }
        })
        .catch(err => {
            console.error("교통 정보 로드 실패:", err);
            subway1.textContent = "에러";
            subway7.textContent = "에러";
            busList.textContent = "에러";
        });
}


// ==========================================
// 4. 밸런스 게임 위젯
// ==========================================
function fetchBalanceGameWidgetData() {
    fetch("/api/widgets/balance-game")
        .then(res => {
            if (res.status === 204) {
                document.getElementById("balance-question-text").textContent = "등록된 밸런스 게임이 없습니다.";
                document.getElementById("balance-vote-buttons").classList.add("hidden");
                document.getElementById("balance-vote-results").classList.add("hidden");
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;

            activeQuestionId = data.id;

            // 질문 주입
            document.getElementById("balance-question-text").textContent = data.question;

            const voteButtonsDiv = document.getElementById("balance-vote-buttons");
            const voteResultsDiv = document.getElementById("balance-vote-results");

            if (data.voted) {
                // 이미 투표 완료한 경우 결과 그래프 노출
                voteButtonsDiv.classList.add("hidden");
                voteResultsDiv.classList.remove("hidden");

                renderBalanceResults(data);
            } else {
                // 아직 투표 안한 경우 선택 버튼 노출
                voteResultsDiv.classList.add("hidden");
                voteButtonsDiv.classList.remove("hidden");

                // 버튼 텍스트 설정
                const buttons = voteButtonsDiv.getElementsByTagName("button");
                buttons[0].textContent = data.optionA;
                buttons[1].textContent = data.optionB;
            }
        })
        .catch(err => console.error("밸런스 게임 로드 실패:", err));
}

// 투표 결과 시각화 헬퍼
function renderBalanceResults(data) {
    document.getElementById("balance-res-a-name").textContent = data.optionA;
    document.getElementById("balance-res-a-pct").textContent = `${data.percentA}% (${data.countA}표)`;
    document.getElementById("balance-bar-a").style.width = `${data.percentA}%`;

    document.getElementById("balance-res-b-name").textContent = data.optionB;
    document.getElementById("balance-res-b-pct").textContent = `${data.percentB}% (${data.countB}표)`;
    document.getElementById("balance-bar-b").style.width = `${data.percentB}%`;

    document.getElementById("balance-total-votes").textContent = data.totalCount;
}

// 투표하기 비동기 요청
function voteBalanceGame(selection) {
    if (!activeQuestionId) return;

    const params = new URLSearchParams();
    params.append("questionId", activeQuestionId);
    params.append("selection", selection);

    fetch("/api/widgets/balance-game/vote", {
        method: "POST",
        body: params
    })
    .then(res => res.json())
    .then(result => {
        if (result.success) {
            // 투표 성공 시 UI 갱신 및 전환
            document.getElementById("balance-vote-buttons").classList.add("hidden");
            
            const resultsDiv = document.getElementById("balance-vote-results");
            resultsDiv.classList.remove("hidden");

            // 결과 매핑 정보 주입을 위해 activeGame의 option 이름 재활용
            const optionA = document.getElementById("balance-vote-buttons").getElementsByTagName("button")[0].textContent;
            const optionB = document.getElementById("balance-vote-buttons").getElementsByTagName("button")[1].textContent;

            const mockData = {
                optionA: optionA,
                optionB: optionB,
                percentA: result.percentA,
                countA: result.countA,
                percentB: result.percentB,
                countB: result.countB,
                totalCount: result.totalCount
            };
            renderBalanceResults(mockData);
        } else {
            alert(result.message || "투표 처리에 실패했습니다.");
        }
    })
    .catch(err => alert("투표 중 네트워크 오류 발생: " + err));
}

// 관리자용 새 밸런스 게임 생성 등록
function handleBalanceCreateSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const data = {};
    formData.forEach((val, key) => data[key] = val);

    fetch("/api/widgets/balance-game/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(result => {
        if (result.success) {
            alert(result.message);
            form.reset();
            closeAdminBalanceModal();
            fetchBalanceGameWidgetData(); // 밸런스 게임 갱신
        } else {
            alert("게임 등록 실패: " + result.message);
        }
    })
    .catch(err => alert("게임 생성 중 오류 발생: " + err));
}
