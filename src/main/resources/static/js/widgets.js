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
let allBalanceGames = [];
let balanceTimer = null;
let balanceProgressInterval = null;
const LOCAL_STORAGE_KEY = "voted_balance_games";

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
function getVotedGamesMap() {
    try {
        return JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY) || "{}");
    } catch (e) {
        return {};
    }
}

function saveVoteToLocalStorage(gameId, selection) {
    const map = getVotedGamesMap();
    map[gameId] = selection;
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(map));
}

function clearBalanceTimer() {
    if (balanceTimer) {
        clearTimeout(balanceTimer);
        balanceTimer = null;
    }
    if (balanceProgressInterval) {
        clearInterval(balanceProgressInterval);
        balanceProgressInterval = null;
    }
}

function fetchBalanceGameWidgetData() {
    clearBalanceTimer();
    
    fetch("/api/widgets/balance-game")
        .then(res => {
            if (res.status === 204) {
                const body = document.getElementById("balance-widget-body");
                if (body) {
                    body.innerHTML = `<h3 class="text-sm font-extrabold text-slate-800 text-center leading-snug">등록된 밸런스 게임이 없습니다.</h3>`;
                }
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;

            allBalanceGames = data;
            const votedMap = getVotedGamesMap();
            
            // 미투표 게임 중 첫 번째(ID 순) 찾기
            const unvoted = allBalanceGames.find(g => !votedMap[g.id]);
            const body = document.getElementById("balance-widget-body");
            if (!body) return;

            if (unvoted) {
                activeQuestionId = unvoted.id;
                
                // 단건 투표 화면 렌더링
                body.innerHTML = `
                    <div class="animate-fade-in-up">
                        <h3 id="balance-question-text" class="text-sm font-extrabold text-slate-800 text-center mb-4 leading-snug">${unvoted.question}</h3>
                        <div id="balance-vote-buttons" class="grid grid-cols-2 gap-3">
                            <button onclick="voteBalanceGame('A')" class="bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 text-indigo-700 font-bold py-2.5 px-3 rounded-2xl transition text-xs text-center line-clamp-2 min-h-[44px]">${unvoted.optionA}</button>
                            <button onclick="voteBalanceGame('B')" class="bg-rose-50 hover:bg-rose-100 border border-rose-200 text-rose-700 font-bold py-2.5 px-3 rounded-2xl transition text-xs text-center line-clamp-2 min-h-[44px]">${unvoted.optionB}</button>
                        </div>
                    </div>
                `;
            } else {
                // 모든 게임 완료 상태 -> 전체 결과 리스트 렌더링
                renderVotedGamesList(allBalanceGames, votedMap);
            }
        })
        .catch(err => {
            console.error("밸런스 게임 로드 실패:", err);
            const body = document.getElementById("balance-widget-body");
            if (body) {
                body.innerHTML = `<h3 class="text-sm font-extrabold text-red-500 text-center leading-snug">밸런스 게임을 불러올 수 없습니다.</h3>`;
            }
        });
}

// 투표하기 비동기 요청
function voteBalanceGame(selection) {
    if (!activeQuestionId) return;
    clearBalanceTimer();

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
                // 1. 로컬스토리지 저장
                saveVoteToLocalStorage(activeQuestionId, selection);

                // 2. 전역 리스트 내 현재 문제 통계 데이터 업데이트
                const gameIdx = allBalanceGames.findIndex(g => g.id === activeQuestionId);
                if (gameIdx !== -1) {
                    allBalanceGames[gameIdx].countA = result.countA;
                    allBalanceGames[gameIdx].countB = result.countB;
                    allBalanceGames[gameIdx].totalCount = result.totalCount;
                    allBalanceGames[gameIdx].percentA = result.percentA;
                    allBalanceGames[gameIdx].percentB = result.percentB;
                }

                // 3. 투표 결과 화면 렌더링
                const curGame = allBalanceGames.find(g => g.id === activeQuestionId);
                const body = document.getElementById("balance-widget-body");
                if (!body || !curGame) return;

                body.innerHTML = `
                    <div class="animate-fade-in-up">
                        <h3 class="text-sm font-extrabold text-slate-800 text-center mb-4 leading-snug">${curGame.question}</h3>
                        
                        <div id="balance-vote-results" class="space-y-3">
                            <div class="space-y-1">
                                <div class="flex justify-between text-xs font-bold text-indigo-700">
                                    <span class="truncate max-w-[150px]">${curGame.optionA}</span>
                                    <span>${result.percentA}% (${result.countA}표)</span>
                                </div>
                                <div class="w-full bg-slate-100 h-3.5 rounded-full overflow-hidden">
                                    <div class="bg-indigo-500 h-full rounded-full transition-all duration-500" style="width: ${result.percentA}%"></div>
                                </div>
                            </div>
                            
                            <div class="space-y-1">
                                <div class="flex justify-between text-xs font-bold text-rose-700">
                                    <span class="truncate max-w-[150px]">${curGame.optionB}</span>
                                    <span>${result.percentB}% (${result.countB}표)</span>
                                </div>
                                <div class="w-full bg-slate-100 h-3.5 rounded-full overflow-hidden">
                                    <div class="bg-rose-500 h-full rounded-full transition-all duration-500" style="width: ${result.percentB}%"></div>
                                </div>
                            </div>
                            
                            <div class="text-[10px] text-slate-400 font-bold text-center mt-1">참여 완료 (총 ${result.totalCount}명 투표)</div>
                        </div>

                        <!-- 다음 질문 자동 이동 타이머 버튼 -->
                        <div id="balance-next-container" class="mt-4 flex justify-center">
                            <button id="balance-next-btn" class="relative overflow-hidden bg-slate-800 hover:bg-slate-900 text-white font-bold py-2 px-5 rounded-2xl transition text-xs shadow-md flex items-center justify-center min-h-[36px] w-full max-w-[200px] select-none">
                                <span class="relative z-10" id="balance-next-btn-text">다음 질문 ➡️ (3초)</span>
                                <div id="balance-next-btn-progress" class="absolute left-0 top-0 bottom-0 bg-indigo-500/40 w-0 transition-all duration-100 ease-linear"></div>
                            </button>
                        </div>
                    </div>
                `;

                // 4. 자동 이동 타이머 구동
                const votedMap = getVotedGamesMap();
                const nextUnvoted = allBalanceGames.find(g => g.id !== activeQuestionId && !votedMap[g.id]);
                const hasMore = !!nextUnvoted;

                const nextBtn = document.getElementById("balance-next-btn");
                const progressEl = document.getElementById("balance-next-btn-progress");
                const textEl = document.getElementById("balance-next-btn-text");

                if (!nextBtn) return;

                const nextText = hasMore ? "다음 질문 ➡️" : "전체 결과 보기 📋";
                textEl.textContent = `${nextText} (3초)`;

                let timeLeft = 3000;
                
                // 즉시 이동 함수
                const triggerNext = () => {
                    clearBalanceTimer();
                    fetchBalanceGameWidgetData();
                };
                
                nextBtn.onclick = triggerNext;

                // 타이머 인터벌
                balanceProgressInterval = setInterval(() => {
                    timeLeft -= 100;
                    const pct = ((3000 - timeLeft) / 3000) * 100;
                    if (progressEl) progressEl.style.width = `${pct}%`;
                    if (textEl) {
                        textEl.textContent = `${nextText} (${Math.ceil(timeLeft / 1000)}초)`;
                    }

                    if (timeLeft <= 0) {
                        triggerNext();
                    }
                }, 100);

                // 마우스 호버 시 타이머 정지 (수동 대기)
                const pauseTimer = () => {
                    clearBalanceTimer();
                    if (progressEl) progressEl.style.width = '0%';
                    if (textEl) textEl.textContent = nextText;
                };

                nextBtn.onmouseenter = pauseTimer;
                nextBtn.ontouchstart = pauseTimer;

            } else {
                alert(result.message || "투표 처리에 실패했습니다.");
            }
        })
        .catch(err => alert("투표 중 네트워크 오류 발생: " + err));
}

// 전체 투표 완료 결과 리스트 렌더링 함수
function renderVotedGamesList(games, votedMap) {
    const body = document.getElementById("balance-widget-body");
    if (!body) return;

    if (games.length === 0) {
        body.innerHTML = `<h3 class="text-sm font-extrabold text-slate-800 text-center leading-snug">등록된 밸런스 게임이 없습니다.</h3>`;
        return;
    }

    // 최신 투표 게임이 위로 오게 리버스 정렬
    const sortedGames = [...games].reverse();

    let listHtml = sortedGames.map(g => {
        const mySelection = votedMap[g.id]; // 'A' or 'B' or undefined
        
        const isASelected = mySelection === 'A';
        const isBSelected = mySelection === 'B';

        // 옵션 A 스타일 정의
        const aClass = isASelected 
            ? "border-indigo-200 bg-indigo-50/30 text-indigo-900" 
            : "border-slate-100 bg-white text-slate-400 opacity-60";
        const aBadge = isASelected 
            ? `<span class="inline-block text-[9px] bg-indigo-100 text-indigo-700 font-extrabold px-1.5 py-0.5 rounded-md ml-1 shadow-sm"><i class="fa-solid fa-check mr-0.5"></i>내 선택</span>` 
            : "";
        const aBarColor = isASelected ? "bg-indigo-500" : "bg-slate-400";

        // 옵션 B 스타일 정의
        const bClass = isBSelected 
            ? "border-rose-200 bg-rose-50/30 text-rose-900" 
            : "border-slate-100 bg-white text-slate-400 opacity-60";
        const bBadge = isBSelected 
            ? `<span class="inline-block text-[9px] bg-rose-100 text-rose-700 font-extrabold px-1.5 py-0.5 rounded-md ml-1 shadow-sm"><i class="fa-solid fa-check mr-0.5"></i>내 선택</span>` 
            : "";
        const bBarColor = isBSelected ? "bg-rose-500" : "bg-slate-400";

        return `
            <div class="border border-slate-100 rounded-2xl p-3 bg-slate-50/50 hover:bg-slate-50/80 transition duration-150 relative">
                <h4 class="font-extrabold text-slate-800 text-xs mb-2 leading-snug"><i class="fa-solid fa-circle-question text-[10px] text-rose-400 mr-1"></i>${g.question}</h4>
                
                <div class="space-y-1.5 text-[10px]">
                    <!-- 옵션 A -->
                    <div class="p-1.5 rounded-xl border transition-all duration-200 ${aClass}">
                        <div class="flex justify-between items-center font-bold">
                            <span class="flex items-center truncate max-w-[150px]">${g.optionA} ${aBadge}</span>
                            <span>${g.percentA}% (${g.countA}표)</span>
                        </div>
                        <div class="w-full bg-slate-200/50 h-1.5 rounded-full overflow-hidden mt-1">
                            <div class="${aBarColor} h-full rounded-full" style="width: ${g.percentA}%"></div>
                        </div>
                    </div>

                    <!-- 옵션 B -->
                    <div class="p-1.5 rounded-xl border transition-all duration-200 ${bClass}">
                        <div class="flex justify-between items-center font-bold">
                            <span class="flex items-center truncate max-w-[150px]">${g.optionB} ${bBadge}</span>
                            <span>${g.percentB}% (${g.countB}표)</span>
                        </div>
                        <div class="w-full bg-slate-200/50 h-1.5 rounded-full overflow-hidden mt-1">
                            <div class="${bBarColor} h-full rounded-full" style="width: ${g.percentB}%"></div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join("");

    body.innerHTML = `
        <div class="animate-fade-in-up flex flex-col h-full">
            <div class="flex justify-between items-center mb-2 text-[10px] text-slate-400 font-bold px-1 select-none">
                <span>📊 전체 투표 결과 리스트</span>
                <span class="text-rose-500 font-extrabold hover:underline cursor-pointer" onclick="resetLocalVotes()">초기화 <i class="fa-solid fa-rotate-left text-[9px]"></i></span>
            </div>
            <div class="space-y-2.5 max-h-[200px] overflow-y-auto pr-1 thin-scrollbar">
                ${listHtml}
            </div>
        </div>
    `;
}

// 로컬 스토리지 초기화 헬퍼 함수
window.resetLocalVotes = function() {
    if (confirm("정말 투표 기록을 초기화하고 처음부터 다시 해보시겠습니까?")) {
        localStorage.removeItem(LOCAL_STORAGE_KEY);
        fetchBalanceGameWidgetData();
    }
};

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
