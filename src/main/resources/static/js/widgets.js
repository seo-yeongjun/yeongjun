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

            const nextRestDayName = data.nextRestDayName;
            const restDayDday = data.restDayDday;
            const countdownHoliday = document.getElementById("countdown-holiday");
            const holidayLabel = document.getElementById("holiday-label");
            if (holidayLabel && countdownHoliday) {
                holidayLabel.textContent = "다음 쉬는날까지";
                if (restDayDday === 0) {
                    countdownHoliday.textContent = `오늘 (${nextRestDayName})`;
                } else {
                    countdownHoliday.textContent = `D-${restDayDday} (${nextRestDayName})`;
                }
            }

            const vacationDday = data.vacationDday;
            const countdownVacation = document.getElementById("countdown-vacation");
            const vacationLabel = document.getElementById("vacation-label");
            if (vacationLabel && countdownVacation) {
                if (vacationDday >= 0) {
                    vacationLabel.textContent = data.dDayLabel;
                    countdownVacation.textContent = vacationDday === 0 ? "D-Day" : `D-${vacationDday}`;
                } else {
                    vacationLabel.textContent = "종강(학기시작)까지";
                    countdownVacation.textContent = "없음";
                }
            }

            // 퇴근/출근 시간 상태 문구 세팅
            const leaveStatus = document.getElementById("leave-status");
            const leaveTimeStr = data.leaveTime.substring(0, 5); // HH:mm
            if (data.isWorkCountdown) {
                leaveStatus.textContent = `09:00 출근 기준`;
                leaveStatus.className = "text-xs text-indigo-600 font-semibold mt-1";
            } else {
                leaveStatus.textContent = `${leaveTimeStr} 퇴근 기준`;
                leaveStatus.className = "text-xs text-indigo-600 font-semibold mt-1";
            }

            // 실시간 타이머 시작
            let secondsLeft = data.secondsToEvent;
            const timerDisplay = document.getElementById("leave-timer");
            const isWorkCountdown = data.isWorkCountdown;

            // 타이틀 엘리먼트 동적 설정
            const titleEl = document.getElementById("countdown-title");
            if (titleEl) {
                const icon = '<i class="fa-solid fa-clock-rotate-left mr-1.5 text-indigo-500"></i>';
                titleEl.innerHTML = icon + (isWorkCountdown ? "출근까지" : "퇴근까지");
            }

            if (countdownInterval) clearInterval(countdownInterval);

            const finishedText = isWorkCountdown ? "출근 완료! 💼" : "퇴근 완료! 🎉";
            const finishedClass = isWorkCountdown
                ? "text-3xl md:text-4xl font-black text-indigo-600 tracking-tight animate-bounce"
                : "text-3xl md:text-4xl font-black text-emerald-600 tracking-tight animate-bounce";

            if (secondsLeft <= 0) {
                timerDisplay.textContent = finishedText;
                timerDisplay.className = finishedClass;
            } else {
                timerDisplay.className = "text-4xl md:text-5xl font-black text-slate-800 tracking-tight";

                function updateTimerDisplay() {
                    if (secondsLeft <= 0) {
                        timerDisplay.textContent = finishedText;
                        timerDisplay.className = finishedClass;
                        clearInterval(countdownInterval);
                        setTimeout(initCountdownWidget, 5000);
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

            const leaveTimeInput = document.getElementById("admin-office-leave-time");
            if (leaveTimeInput) {
                leaveTimeInput.value = data.leaveTimeNormal || "18:00:00";
            }
            form.elements["VACATION_START_DATE"].value = data.vacationStartDate || "";
            form.elements["SEMESTER_START_DATE"].value = data.semesterStartDate || "";

            // 공휴일 목록 로드
            loadHolidays();
        });
}

// 관리자용 퇴근 설정 변경 전송
function handleCountdownConfigSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);
    const data = {};
    formData.forEach((val, key) => data[key] = val);

    const leaveTimeInput = document.getElementById("admin-office-leave-time");
    if (leaveTimeInput) {
        const leaveTimeVal = leaveTimeInput.value || "18:00:00";
        data["OFFICE_LEAVE_TIME_NORMAL"] = leaveTimeVal;
        data["OFFICE_LEAVE_TIME_VACATION"] = leaveTimeVal;
    }

    fetch("/api/widgets/countdown/config", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) {
                alert("설정이 성공적으로 저장되었습니다.");
                closeAdminCountdownModal();
                initCountdownWidget(); // 위젯 새로고침
            } else {
                res.text().then(text => alert("설정 저장 실패: " + text));
            }
        })
        .catch(err => alert("설정 저장 중 오류가 발생했습니다: " + err));
}

// 공휴일 목록 로드
function loadHolidays() {
    const container = document.getElementById("holiday-list-container");
    if (!container) return;

    fetch("/api/widgets/countdown/holidays")
        .then(res => res.json())
        .then(list => {
            if (list.length === 0) {
                container.innerHTML = `<div class="text-slate-400 text-center py-4 font-semibold">등록된 공휴일이 없습니다.</div>`;
                return;
            }
            container.innerHTML = list.map(h => `
                <div class="flex justify-between items-center bg-slate-50 border border-slate-100 px-3 py-2 rounded-xl">
                    <span class="font-extrabold text-slate-700">${h.holidayDate} (${h.holidayName})</span>
                    <button type="button" onclick="handleDeleteHoliday('${h.holidayDate}')" class="text-rose-500 hover:text-rose-700 transition">
                        <i class="fa-solid fa-trash-can text-sm"></i>
                    </button>
                </div>
            `).join('');
        })
        .catch(err => {
            console.error("공휴일 목록 로드 실패:", err);
            container.innerHTML = `<div class="text-rose-500 text-center py-2 font-semibold">로드 중 오류가 발생했습니다.</div>`;
        });
}

// 공휴일 추가
function handleAddHoliday() {
    const dateInput = document.getElementById("new-holiday-date");
    const nameInput = document.getElementById("new-holiday-name");
    if (!dateInput || !nameInput) return;

    const holidayDate = dateInput.value;
    const holidayName = nameInput.value.trim();

    if (!holidayDate || !holidayName) {
        alert("날짜와 공휴일 이름을 모두 입력해주세요.");
        return;
    }

    fetch("/api/widgets/countdown/holidays", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ holidayDate, holidayName })
    })
        .then(res => {
            if (res.ok) {
                dateInput.value = "";
                nameInput.value = "";
                loadHolidays();
                initCountdownWidget(); // 위젯 새로고침
            } else {
                res.text().then(text => alert("공휴일 추가 실패: " + text));
            }
        })
        .catch(err => alert("공휴일 추가 중 오류 발생: " + err));
}

// 공휴일 삭제
function handleDeleteHoliday(date) {
    if (!confirm(`${date} 공휴일을 삭제하시겠습니까?`)) return;

    fetch(`/api/widgets/countdown/holidays?date=${date}`, {
        method: "DELETE"
    })
        .then(res => {
            if (res.ok) {
                loadHolidays();
                initCountdownWidget(); // 위젯 새로고침
            } else {
                res.text().then(text => alert("공휴일 삭제 실패: " + text));
            }
        })
        .catch(err => alert("공휴일 삭제 중 오류 발생: " + err));
}


// ==========================================
// 2. 날씨 위젯
// ==========================================
function fetchWeatherWidgetData() {
    fetch("/api/widgets/weather")
        .then(res => {
            if (!res.ok) throw new Error("HTTP error");
            return res.json();
        })
        .then(data => {
            if (!data || data.currentTemp === undefined) {
                showWeatherError();
                return;
            }
            document.getElementById("weather-current-temp").textContent = data.currentTemp;
            document.getElementById("weather-current-status").textContent = data.currentStatus;

            const popElem = document.getElementById("weather-current-pop");
            if (popElem) {
                popElem.textContent = data.currentPop !== undefined ? data.currentPop : 0;
            }

            const humidityElem = document.getElementById("weather-current-humidity");
            if (humidityElem) {
                humidityElem.textContent = data.currentHumidity !== undefined ? data.currentHumidity : "-";
            }

            const windElem = document.getElementById("weather-current-wind");
            if (windElem) {
                windElem.textContent = data.currentWindSpeed !== undefined ? data.currentWindSpeed : "-";
            }

            const minTempElem = document.getElementById("weather-temp-min");
            if (minTempElem) {
                minTempElem.textContent = data.todayMinTemp !== undefined ? data.todayMinTemp : "-";
            }

            const maxTempElem = document.getElementById("weather-temp-max");
            if (maxTempElem) {
                maxTempElem.textContent = data.todayMaxTemp !== undefined ? data.todayMaxTemp : "-";
            }

            // 날씨 아이콘 변경
            const iconElem = document.getElementById("weather-main-icon");
            if (iconElem) {
                iconElem.className = `fa-solid ${data.currentIcon} text-5xl filter drop-shadow-sm`;
            }

            // 시간대별 예보 렌더링
            const hourlyList = document.getElementById("weather-hourly-list");
            if (hourlyList) {
                hourlyList.innerHTML = ""; // 초기화
                if (data.forecastHours && data.forecastHours.length > 0) {
                    data.forecastHours.forEach(hour => {
                        const hourDiv = document.createElement("div");
                        hourDiv.className = "flex flex-col items-center flex-shrink-0 text-center min-w-[50px] p-1";
                        hourDiv.innerHTML = `
                            <span class="text-[10px] text-slate-400 font-semibold">${hour.time}</span>
                            <i class="fa-solid ${hour.icon} text-lg my-1.5"></i>
                            <span class="text-xs font-bold text-slate-700">${hour.temp}°</span>
                            <span class="text-[9px] font-semibold text-sky-500 mt-0.5"><i class="fa-solid fa-umbrella text-[8px] mr-0.5"></i>${hour.pop}%</span>
                        `;
                        hourlyList.appendChild(hourDiv);
                    });
                }
            }
        })
        .catch(err => {
            console.error("날씨 정보 로드 실패:", err);
            showWeatherError();
        });
}

function showWeatherError() {
    const tempElem = document.getElementById("weather-current-temp");
    if (tempElem) tempElem.textContent = "-";
    const statusElem = document.getElementById("weather-current-status");
    if (statusElem) {
        statusElem.textContent = "업데이트 할 수 없음";
    }
    const popElem = document.getElementById("weather-current-pop");
    if (popElem) popElem.textContent = "-";
    const humidityElem = document.getElementById("weather-current-humidity");
    if (humidityElem) humidityElem.textContent = "-";
    const windElem = document.getElementById("weather-current-wind");
    if (windElem) windElem.textContent = "-";
    const minTempElem = document.getElementById("weather-temp-min");
    if (minTempElem) minTempElem.textContent = "-";
    const maxTempElem = document.getElementById("weather-temp-max");
    if (maxTempElem) maxTempElem.textContent = "-";

    const iconElem = document.getElementById("weather-main-icon");
    if (iconElem) {
        iconElem.className = "fa-solid fa-triangle-exclamation text-5xl text-amber-500 filter drop-shadow-sm";
    }
    const hourlyList = document.getElementById("weather-hourly-list");
    if (hourlyList) {
        hourlyList.innerHTML = `<div class="w-full text-center text-xs text-red-400 font-extrabold py-2">업데이트 할 수 없음</div>`;
    }
}


// ==========================================
// 3. 대중교통 정보 위젯
// ==========================================
function fetchTransitWidgetData() {
    const subway1 = document.getElementById("transit-subway1-info");
    const subway7 = document.getElementById("transit-subway7-info");
    const bus17999 = document.getElementById("transit-bus-17999");
    const bus17117 = document.getElementById("transit-bus-17117");
    const bus17682 = document.getElementById("transit-bus-17682");

    if (subway1) subway1.textContent = "업데이트 중...";
    if (subway7) subway7.textContent = "업데이트 중...";
    const loaderHtml = `<span class="text-[10px] text-slate-400 font-bold">로딩 중...</span>`;
    if (bus17999) bus17999.innerHTML = loaderHtml;
    if (bus17117) bus17117.innerHTML = loaderHtml;
    if (bus17682) bus17682.innerHTML = loaderHtml;

    fetch("/api/widgets/transit")
        .then(res => res.json())
        .then(data => {
            // 지하철 렌더링 헬퍼
            const renderSubwayList = (element, arrivals, badgeColorClass, textColorClass) => {
                if (!element) return;
                element.innerHTML = "";
                if (arrivals === null) {
                    element.innerHTML = `<span class="text-[10px] text-red-400 font-extrabold">업데이트 할 수 없음</span>`;
                    return;
                }
                if (arrivals && arrivals.length > 0) {
                    const downTrains = arrivals.filter(t => t.direction === "하행");
                    const upTrains = arrivals.filter(t => t.direction === "상행");

                    let html = "";

                    // 하행
                    const downTrainsStr = downTrains.length > 0
                        ? downTrains.slice(0, 2).map(t => `<span class="font-extrabold text-slate-700">${t.destination}</span> <span class="font-black ${textColorClass}">${t.arrivalTime}</span>`).join(", ")
                        : `<span class="text-slate-400">도착 정보 없음</span>`;
                    html += `
                        <div class="flex items-center justify-end space-x-1.5 mb-1">
                            <span class="text-[9px] px-1 py-0.2 rounded font-bold ${badgeColorClass}">하행</span>
                            <span class="text-slate-600">${downTrainsStr}</span>
                        </div>
                    `;

                    // 상행
                    const upTrainsStr = upTrains.length > 0
                        ? upTrains.slice(0, 2).map(t => `<span class="font-extrabold text-slate-700">${t.destination}</span> <span class="font-black ${textColorClass}">${t.arrivalTime}</span>`).join(", ")
                        : `<span class="text-slate-400">도착 정보 없음</span>`;
                    html += `
                        <div class="flex items-center justify-end space-x-1.5">
                            <span class="text-[9px] px-1 py-0.2 rounded font-bold ${badgeColorClass}">상행</span>
                            <span class="text-slate-600">${upTrainsStr}</span>
                        </div>
                    `;
                    element.innerHTML = html;
                } else {
                    element.innerHTML = `<span class="text-[10px] text-slate-400 font-bold">도착 정보 없음</span>`;
                }
            };

            renderSubwayList(subway1, data.subwayLine1, "bg-blue-100 text-blue-700", "text-blue-600");
            renderSubwayList(subway7, data.subwayLine7, "bg-green-100 text-green-700", "text-emerald-600");

            // 정류소별 고정 정차 버스 목록
            const FIXED_BUS_LISTS = {
                "transit-bus-17999": ["10", "52", "57", "57-1", "75", "83", "88"],
                "transit-bus-17117": ["10", "52", "57", "57-1", "75", "83", "88", "660", "구로07"],
                "transit-bus-17682": ["660", "6614", "구로07"]
            };

            const BUS_DEFAULT_TYPES = {
                "660": "지선",
                "6614": "지선",
                "구로07": "지선"
            };

            // 버스 렌더링 헬퍼
            const renderBusList = (element, arrivals) => {
                if (!element) return;
                element.innerHTML = "";
                if (arrivals === null) {
                    element.innerHTML = `<span class="text-[10px] text-red-400 font-extrabold whitespace-nowrap">업데이트 할 수 없음</span>`;
                    return;
                }

                // 해당 엘리먼트의 고정 버스 목록 가져오기
                const fixedList = FIXED_BUS_LISTS[element.id] || [];
                const realArrivals = arrivals || [];

                fixedList.forEach(busNo => {
                    // 실시간 정보에서 일치하는 버스 찾기
                    const matchedBus = realArrivals.find(b => b.busNo === busNo);
                    
                    const busItem = document.createElement("div");
                    busItem.className = "flex flex-col items-center justify-center flex-shrink-0 bg-slate-50/70 hover:bg-slate-100/70 border border-slate-100 rounded-xl px-2 py-0.5 min-w-[76px] transition-colors duration-150";

                    let busType = "일반";
                    let arrivalTime1 = "도착 정보 없음";
                    let arrivalTime2 = "-";

                    if (matchedBus) {
                        busType = matchedBus.type || "일반";
                        arrivalTime1 = matchedBus.arrivalTime1 || "도착 정보 없음";
                        arrivalTime2 = matchedBus.arrivalTime2 || "-";
                    } else {
                        busType = BUS_DEFAULT_TYPES[busNo] || "일반";
                    }

                    let badgeClass = "bg-slate-200 text-slate-700";
                    if (busType === "지선") badgeClass = "bg-green-100 text-green-700";
                    else if (busType === "간선") badgeClass = "bg-blue-100 text-blue-700";
                    else if (busType === "순환") badgeClass = "bg-yellow-100 text-yellow-700";
                    else if (busType === "광역") badgeClass = "bg-red-100 text-red-700";

                    const time1ColorClass = arrivalTime1 === "도착 정보 없음" ? "text-slate-400 font-medium text-[8px] tracking-tight" : "font-black text-rose-500 text-[10px] tracking-tight";
                    
                    const time2Html = arrivalTime2 && arrivalTime2 !== "-"
                        ? `<span class="text-[9px] text-slate-400 font-bold ml-0.5">(${arrivalTime2})</span>`
                        : "";

                    busItem.innerHTML = `
                        <div class="flex items-center space-x-1 whitespace-nowrap">
                            <span class="font-bold text-slate-700 text-[10px]">${busNo}번</span>
                            <span class="text-[8px] px-0.5 py-0 rounded font-black ${badgeClass}">${busType}</span>
                        </div>
                        <div class="flex items-center justify-center whitespace-nowrap">
                            <span class="${time1ColorClass}">${arrivalTime1}</span>${time2Html}
                        </div>
                    `;
                    element.appendChild(busItem);
                });
            };

            // 각 정류장별 버스 리스트 렌더링
            renderBusList(bus17999, data.bus17999);
            renderBusList(bus17117, data.bus17117);
            renderBusList(bus17682, data.bus17682);
        })
        .catch(err => {
            console.error("교통 정보 로드 실패:", err);
            if (subway1) subway1.textContent = "에러";
            if (subway7) subway7.textContent = "에러";
            const errHtml = `<span class="text-[10px] text-red-500 font-bold">에러</span>`;
            if (bus17999) bus17999.innerHTML = errHtml;
            if (bus17117) bus17117.innerHTML = errHtml;
            if (bus17682) bus17682.innerHTML = errHtml;
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
