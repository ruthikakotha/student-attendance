import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../theme";

const API_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");

function Login() {
    const [role, setRole] = useState("teacher");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const { theme, toggleTheme } = useTheme();

    const handleLogin = async (e) => {
        e.preventDefault();

        if (role === "teacher") {
            if (username === "admin" && password === "admin123") {
                navigate("/dashboard");
            } else {
                alert("Invalid teacher username or password");
            }
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(`${API_URL}/student-login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    rollNo: username.trim(),
                    password,
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                alert(data.message || "Invalid student credentials");
                return;
            }

            sessionStorage.setItem("student", JSON.stringify(data));
            navigate("/student-dashboard");
        } catch (error) {
            console.error("Login error:", error);
            alert("Unable to connect to the server.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <button
                type="button"
                className="theme-toggle login-theme"
                onClick={toggleTheme}
            >
                {theme === "light" ? "☾ Dark mode" : "☀ Light mode"}
            </button>

            <form onSubmit={handleLogin} className="login-card">
                <h1>Student Attendance</h1>

                <p className="login-subtitle">
                    Attendance Management System
                </p>

                <div className="role-switcher">
                    <button
                        type="button"
                        onClick={() => {
                            setRole("teacher");
                            setUsername("");
                            setPassword("");
                        }}
                        className={
                            role === "teacher"
                                ? "role-switch active"
                                : "role-switch"
                        }
                    >
                        Teacher
                    </button>

                    <button
                        type="button"
                        onClick={() => {
                            setRole("student");
                            setUsername("");
                            setPassword("");
                        }}
                        className={
                            role === "student"
                                ? "role-switch active"
                                : "role-switch"
                        }
                    >
                        Student
                    </button>
                </div>

                <h2>
                    {role === "teacher" ? "Teacher Login" : "Student Login"}
                </h2>

                <input
                    type="text"
                    placeholder={
                        role === "teacher"
                            ? "Username"
                            : "Student Roll Number"
                    }
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>
        </div>
    );
}

export default Login;