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
        setLoading(true);

        try {
            const endpoint =
                role === "teacher" ? "teacher-login" : "student-login";

            const body =
                role === "teacher"
                    ? { username: username.trim(), password }
                    : { rollNo: username.trim(), password };

            const response = await fetch(`${API_URL}/${endpoint}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body),
            });

            const data = await response.json();

            if (!response.ok) {
                alert(data.message || "Invalid login credentials");
                return;
            }

            if (!data.token) {
                alert("Login failed: authentication token was not returned.");
                return;
            }

            sessionStorage.setItem("token", data.token);
            sessionStorage.setItem("role", data.role);

            if (role === "student") {
                sessionStorage.setItem(
                    "student",
                    JSON.stringify(data)
                );
                navigate("/student-dashboard");
            } else {
                navigate("/dashboard");
            }
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
                {theme === "light" ? "Dark mode" : "Light mode"}
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
                    autoComplete="username"
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete={
                        role === "teacher" ? "current-password" : "current-password"
                    }
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>
        </div>
    );
}

export default Login;