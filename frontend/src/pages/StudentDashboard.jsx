import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../theme";

const API_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");

function StudentDashboard() {
    const navigate = useNavigate();
    const { theme, toggleTheme } = useTheme();

    const [student, setStudent] = useState(null);
    const [attendance, setAttendance] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = sessionStorage.getItem("token");
        const role = sessionStorage.getItem("role");
        const storedStudent = sessionStorage.getItem("student");

        if (!token || role !== "STUDENT" || !storedStudent) {
            sessionStorage.clear();
            navigate("/", { replace: true });
            return;
        }

        let currentStudent;

        try {
            currentStudent = JSON.parse(storedStudent);

            if (!currentStudent?.id) {
                sessionStorage.clear();
                navigate("/", { replace: true });
                return;
            }
        } catch {
            sessionStorage.clear();
            navigate("/", { replace: true });
            return;
        }

        setStudent(currentStudent);

        const loadAttendance = async () => {
            try {
                const response = await fetch(
                    `${API_URL}/attendance/student/${currentStudent.id}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );

                if (response.status === 401 || response.status === 403) {
                    sessionStorage.clear();
                    navigate("/", { replace: true });
                    return;
                }

                if (!response.ok) {
                    throw new Error("Failed to load attendance");
                }

                const data = await response.json();
                setAttendance(Array.isArray(data) ? data : []);
            } catch (error) {
                console.error("Attendance loading error:", error);
                alert("Unable to load your attendance.");
            } finally {
                setLoading(false);
            }
        };

        loadAttendance();
    }, [navigate]);

    const handleLogout = () => {
        sessionStorage.clear();
        navigate("/", { replace: true });
    };

    if (!student || loading) {
        return (
            <div className="page">
                <h1>Student Dashboard</h1>
                <p>Loading your dashboard...</p>
            </div>
        );
    }

    const presentCount = attendance.filter(
        item => item.status?.toLowerCase() === "present"
    ).length;

    const absentCount = attendance.filter(
        item => item.status?.toLowerCase() === "absent"
    ).length;

    const total = presentCount + absentCount;

    const percentage = total > 0
        ? Math.round((presentCount / total) * 100)
        : 0;

    const subjects = {};

    attendance.forEach(item => {
        const originalSubject = item.subject?.trim() || "Other";
        const subjectKey = originalSubject.toLowerCase();

        if (!subjects[subjectKey]) {
            subjects[subjectKey] = {
                name: originalSubject,
                present: 0,
                absent: 0,
            };
        }

        if (item.status?.toLowerCase() === "present") {
            subjects[subjectKey].present++;
        } else if (item.status?.toLowerCase() === "absent") {
            subjects[subjectKey].absent++;
        }
    });

    return (
        <>
            <nav className="navbar">
                <div className="navbar-title">Student Portal</div>

                <div className="student-nav-actions">
                    <button
                        type="button"
                        className="theme-toggle"
                        onClick={toggleTheme}
                        aria-label="Toggle color theme"
                    >
                        {theme === "light" ? "☾ Dark mode" : "☀ Light mode"}
                    </button>

                    <button
                        type="button"
                        onClick={handleLogout}
                        style={{
                            padding: "10px 18px",
                            border: "1px solid white",
                            borderRadius: "8px",
                            background: "transparent",
                            color: "white",
                            fontWeight: "bold",
                            cursor: "pointer",
                        }}
                    >
                        Logout
                    </button>
                </div>
            </nav>

            <main className="page student-page">
                <div className="student-page-heading">
                    <div>
                        <h1>Welcome, {student.name}!</h1>
                        <p>Here is your attendance summary.</p>
                    </div>

                    <span className="portal-label">Student portal</span>
                </div>

                <div className="dashboard-cards student-details">
                    <div className="dashboard-card">
                        <h3>Roll Number</h3>
                        <h2>{student.rollNo}</h2>
                    </div>

                    <div className="dashboard-card">
                        <h3>Department</h3>
                        <h2>{student.department}</h2>
                    </div>

                    <div className="dashboard-card">
                        <h3>Year &amp; Section</h3>
                        <h2>{student.year} / {student.section}</h2>
                    </div>
                </div>

                <div
                    className="dashboard-stats student-attendance-stats"
                    style={{ marginTop: "25px" }}
                >
                    <div className="stat-card">
                        <div>
                            <p>Total Classes</p>
                            <h2>{total}</h2>
                        </div>
                    </div>

                    <div className="stat-card present-card">
                        <div>
                            <p>Present</p>
                            <h2>{presentCount}</h2>
                        </div>
                    </div>

                    <div className="stat-card absent-card">
                        <div>
                            <p>Absent</p>
                            <h2>{absentCount}</h2>
                        </div>
                    </div>

                    <div className="stat-card percentage-card">
                        <div>
                            <p>Attendance</p>
                            <h2>{percentage}%</h2>

                            <div
                                className="progress-track"
                                role="progressbar"
                                aria-label="Your overall attendance"
                                aria-valuemin="0"
                                aria-valuemax="100"
                                aria-valuenow={percentage}
                            >
                                <span
                                    className="progress-fill"
                                    style={{ width: `${percentage}%` }}
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <h2 style={{ marginTop: "35px" }}>
                    Subject-wise Attendance
                </h2>

                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Subject</th>
                                <th>Present</th>
                                <th>Absent</th>
                                <th>Total Classes</th>
                                <th>Percentage</th>
                            </tr>
                        </thead>

                        <tbody>
                            {Object.keys(subjects).length === 0 ? (
                                <tr>
                                    <td colSpan="5">
                                        No attendance records available.
                                    </td>
                                </tr>
                            ) : (
                                Object.entries(subjects).map(
                                    ([subjectKey, data]) => {
                                        const subjectTotal =
                                            data.present + data.absent;

                                        const subjectPercentage =
                                            subjectTotal > 0
                                                ? Math.round(
                                                    (data.present / subjectTotal) * 100
                                                )
                                                : 0;

                                        return (
                                            <tr key={subjectKey}>
                                                <td>{data.name}</td>
                                                <td>{data.present}</td>
                                                <td>{data.absent}</td>
                                                <td>{subjectTotal}</td>
                                                <td>
                                                    <div>
                                                        {subjectPercentage}%
                                                    </div>
                                                    <div className="subject-progress">
                                                        <span
                                                            style={{
                                                                width: `${subjectPercentage}%`,
                                                            }}
                                                        />
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    }
                                )
                            )}
                        </tbody>
                    </table>
                </div>
            </main>
        </>
    );
}

export default StudentDashboard;