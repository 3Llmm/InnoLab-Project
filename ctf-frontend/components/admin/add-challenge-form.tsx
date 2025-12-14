"use client"

import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Switch } from "@/components/ui/switch"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Terminal, Monitor, Code, Server, FileText, X, Upload, CheckCircle2 } from "lucide-react"
import { createChallenge } from "@/lib/api/admin"
import { useToast } from "@/hooks/use-toast"
import { Badge } from "@/components/ui/badge"

// Form validation schema
const challengeFormSchema = z.object({
    title: z.string().min(1, "Title is required"),
    description: z.string().min(1, "Description is required"),
    category: z.string().min(1, "Category is required"),
    difficulty: z.enum(["easy", "medium", "hard"]),
    points: z.number().min(1, "Points must be at least 1"),
    flag: z.string().optional(),

    // Download file (optional for instance challenges)
    file: z
        .any()
        .refine((val) => {
            // If no file selected, that's okay (optional)
            if (!val) return true;
            // If file selected, validate it's a File
            return val instanceof File;
        }, "Please select a valid file")
        .optional(),

    // Instance fields
    requiresInstance: z.boolean().default(false),
    dockerImageName: z.string().optional(),
    
    // Hints
    hints: z.array(z.string()).optional(),
})

type ChallengeFormValues = z.infer<typeof challengeFormSchema>

// Accepted file types for Docker challenges
const ACCEPTED_DOCKER_FILES = [
    ".dockerfile",
    ".Dockerfile",
    ".sh",
    ".c",
    ".cpp",
    ".py",
    ".js",
    ".txt",
    ".md",
    ".yml",
    ".yaml",
    ".json"
]

export default function AddChallengeForm() {
    const [isLoading, setIsLoading] = useState(false)
    const [dockerFiles, setDockerFiles] = useState<File[]>([])
    const [hints, setHints] = useState<string[]>([])
    const [newHint, setNewHint] = useState("")
    const [showSuccessAlert, setShowSuccessAlert] = useState(false)
    const { toast } = useToast()

    const form = useForm<ChallengeFormValues>({
        resolver: zodResolver(challengeFormSchema),
        defaultValues: {
            title: "",
            description: "",
            category: "",
            difficulty: "easy",
            points: 100,
            flag: "",
            requiresInstance: false,
            dockerImageName: "",
        },
    })

    const requiresInstance = form.watch("requiresInstance")

    const handleDockerFileChange = (files: FileList | null) => {
        if (!files) return;

        const newFiles = Array.from(files).filter(file => {
            const name = file.name;
            const lower = name.toLowerCase();

            // Detect extension safely
            const dotIndex = name.lastIndexOf('.');
            const extension = dotIndex !== -1 ? lower.substring(dotIndex) : '';

            // Accept if extension matches OR filename is literally "Dockerfile"
            const isDockerfile = name === "Dockerfile" || lower === "dockerfile";

            return ACCEPTED_DOCKER_FILES.includes(extension) || isDockerfile;
        });

        setDockerFiles(prev => [...prev, ...newFiles]);
    };


    const removeDockerFile = (index: number) => {
        setDockerFiles(prev => prev.filter((_, i) => i !== index))
    }

    const getFileIcon = (fileName: string) => {
        if (fileName === "Dockerfile" || fileName.endsWith(".dockerfile")) {
            return "🐳"
        } else if (fileName.endsWith(".sh")) {
            return "📜"
        } else if (fileName.endsWith(".c") || fileName.endsWith(".cpp")) {
            return "⚙️"
        } else if (fileName.endsWith(".py")) {
            return "🐍"
        } else if (fileName.endsWith(".js")) {
            return "📘"
        } else if (fileName.endsWith(".md")) {
            return "📝"
        } else {
            return "📄"
        }
    }

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return '0 Bytes'
        const k = 1024
        const sizes = ['Bytes', 'KB', 'MB']
        const i = Math.floor(Math.log(bytes) / Math.log(k))
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const onSubmit = async (data: ChallengeFormValues) => {
        setIsLoading(true)
        setShowSuccessAlert(false)

        try {
            const formData = new FormData()

            // Add basic form data
            formData.append("title", data.title)
            formData.append("description", data.description)
            formData.append("category", data.category)
            formData.append("difficulty", data.difficulty)
            formData.append("points", data.points.toString())

            if (data.flag) {
                formData.append("flag", data.flag)
            }

            // Add download file if provided
            if (data.file) {
                formData.append("downloadFile", data.file)
            }

            // Add instance configuration
            formData.append("requiresInstance", data.requiresInstance.toString())
            if (data.requiresInstance) {
                if (data.dockerImageName) {
                    formData.append("dockerImageName", data.dockerImageName)
                }

                // Add Docker files
                dockerFiles.forEach((file, index) => {
                    formData.append(`dockerFiles`, file)
                })
            }

            // Add hints
            hints.forEach((hint, index) => {
                formData.append(`hints`, hint)
            })

            await createChallenge(formData)

            // Show success toast
            toast({
                title: "✅ Challenge Created Successfully!",
                description: `"${data.title}" has been added to the platform and is now available to players.`,
                duration: 5000,
            })

            // Show success alert banner
            setShowSuccessAlert(true)
            setTimeout(() => setShowSuccessAlert(false), 5000)

            // Reset form
            form.reset({
                title: "",
                description: "",
                category: "",
                difficulty: "easy",
                points: 100,
                flag: "",
                requiresInstance: false,
                dockerImageName: "",
                defaultSshPort: 30000,
                defaultVscodePort: 31000,
                defaultDesktopPort: 32000,
            })
            setDockerFiles([])

            // Scroll to top to show success message
            window.scrollTo({ top: 0, behavior: 'smooth' })

        } catch (error) {
            console.error("Challenge creation error:", error)
            
            toast({
                title: "❌ Error Creating Challenge",
                description: error instanceof Error ? error.message : "An unexpected error occurred. Please try again.",
                variant: "destructive",
                duration: 7000,
            })
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className="space-y-4">
            {/* Success Alert Banner */}
            {showSuccessAlert && (
                <Alert className="bg-green-50 border-green-200 animate-in fade-in slide-in-from-top-2 duration-300">
                    <CheckCircle2 className="h-5 w-5 text-green-600" />
                    <AlertDescription className="text-green-800 font-medium">
                        Challenge created successfully! The challenge is now live on the platform.
                    </AlertDescription>
                </Alert>
            )}

            <Card>
                <CardHeader>
                    <CardTitle>Create New Challenge</CardTitle>
                    <CardDescription>
                        Add a new CTF challenge. Choose between static download or Docker instance-based challenges.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <div onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            {/* Basic Information */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <FormField
                                    control={form.control}
                                    name="title"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Title</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Buffer Overflow Challenge" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="category"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Category</FormLabel>
                                            <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Select a category" />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value="binary-exploitation">Binary Exploitation</SelectItem>
                                                    <SelectItem value="web-exploitation">Web Exploitation</SelectItem>
                                                    <SelectItem value="cryptography">Cryptography</SelectItem>
                                                    <SelectItem value="reverse-engineering">Reverse Engineering</SelectItem>
                                                    <SelectItem value="forensics">Forensics</SelectItem>
                                                    <SelectItem value="linux-basics">Linux Basics</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            <FormField
                                control={form.control}
                                name="description"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Description</FormLabel>
                                        <FormControl>
                                            <Textarea
                                                placeholder="Describe the challenge and what players need to do..."
                                                className="min-h-[100px]"
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <FormField
                                    control={form.control}
                                    name="difficulty"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Difficulty</FormLabel>
                                            <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                <FormControl>
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Select difficulty" />
                                                    </SelectTrigger>
                                                </FormControl>
                                                <SelectContent>
                                                    <SelectItem value="easy">Easy</SelectItem>
                                                    <SelectItem value="medium">Medium</SelectItem>
                                                    <SelectItem value="hard">Hard</SelectItem>
                                                </SelectContent>
                                            </Select>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="points"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Points</FormLabel>
                                            <FormControl>
                                                <Input
                                                    type="number"
                                                    placeholder="100"
                                                    {...field}
                                                    onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="flag"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Flag (Optional for Docker)</FormLabel>
                                            <FormControl>
                                                <Input placeholder="FLAG{example_flag}" {...field} />
                                            </FormControl>
                                            <FormDescription>
                                                For Docker challenges, flag is usually generated automatically
                                            </FormDescription>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </div>

                            {/* Download File (Optional for instance challenges) */}
                            <FormField
                                control={form.control}
                                name="file"
                                render={({ field: { value, onChange, ...field } }) => (
                                    <FormItem>
                                        <FormLabel>Download File (Optional)</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="file"
                                                accept="*"
                                                onChange={(e) => onChange(e.target.files?.[0])}
                                                {...field}
                                            />
                                        </FormControl>
                                        <FormDescription>
                                            Upload files for static challenges (zip, pdf, etc.)
                                        </FormDescription>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            {/* Instance Configuration */}
                            <div className="space-y-4">
                                <FormField
                                    control={form.control}
                                    name="requiresInstance"
                                    render={({ field }) => (
                                        <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                            <div className="space-y-0.5">
                                                <FormLabel className="text-base">Docker Instance Required</FormLabel>
                                                <FormDescription>
                                                    Enable for challenges that need a Docker container with SSH access
                                                </FormDescription>
                                            </div>
                                            <FormControl>
                                                <Switch
                                                    checked={field.value}
                                                    onCheckedChange={field.onChange}
                                                />
                                            </FormControl>
                                        </FormItem>
                                    )}
                                />

                                {requiresInstance && (
                                    <Alert className="bg-blue-50 border-blue-200">
                                        <Server className="h-4 w-4 text-blue-600" />
                                        <AlertDescription className="text-blue-700">
                                            Docker instance enabled. Players will get a dedicated container with SSH access.
                                        </AlertDescription>
                                    </Alert>
                                )}

                                {requiresInstance && (
                                    <div className="space-y-6 border rounded-lg p-6 bg-muted/30">
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                            <FormField
                                                control={form.control}
                                                name="dockerImageName"
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>Docker Image Name</FormLabel>
                                                        <FormControl>
                                                            <Input
                                                                placeholder="myctf/pwn-stack0"
                                                                {...field}
                                                            />
                                                        </FormControl>
                                                        <FormDescription>
                                                            Optional: Name for the Docker image
                                                        </FormDescription>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />

                                            <div className="space-y-4">
                                                {/* Port fields removed - allocation happens instantly on server */}
                                            </div>
                                        </div>

                                        {/* Docker Files Upload Section */}
                                        <div className="space-y-4 pt-4 border-t">
                                            <div className="space-y-2">
                                                <FormLabel className="flex items-center gap-2">
                                                    <FileText className="h-4 w-4" />
                                                    Docker Challenge Files
                                                </FormLabel>
                                                <FormDescription>
                                                    Upload Dockerfile, source code, scripts, and other files needed for the challenge
                                                </FormDescription>
                                            </div>

                                            <div className="border-2 border-dashed border-muted-foreground/25 rounded-lg p-6 hover:border-primary/50 transition-colors">
                                                <div className="flex flex-col items-center justify-center space-y-4">
                                                    <Upload className="h-8 w-8 text-muted-foreground" />
                                                    <div className="text-center">
                                                        <p className="text-sm font-medium">Drag & drop files here, or click to browse</p>
                                                        <p className="text-xs text-muted-foreground mt-1">
                                                            Accepted: Dockerfile, .sh, .c, .cpp, .py, .js, .txt, .md
                                                        </p>
                                                    </div>
                                                    <Input
                                                        type="file"
                                                        multiple
                                                        accept=".dockerfile,.Dockerfile,.sh,.c,.cpp,.py,.js,.txt,.md,.yml,.yaml,.json,*/*"
                                                        onChange={(e) => handleDockerFileChange(e.target.files)}
                                                    />
                                                </div>
                                            </div>

                                            {/* Uploaded Files List */}
                                            {dockerFiles.length > 0 && (
                                                <div className="space-y-3">
                                                    <div className="flex items-center justify-between">
                                                        <p className="text-sm font-medium">
                                                            Uploaded Files ({dockerFiles.length})
                                                        </p>
                                                        <Button
                                                            type="button"
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() => setDockerFiles([])}
                                                            className="text-destructive hover:text-destructive/80"
                                                        >
                                                            Clear All
                                                        </Button>
                                                    </div>

                                                    <div className="space-y-2 max-h-60 overflow-y-auto">
                                                        {dockerFiles.map((file, index) => (
                                                            <div
                                                                key={index}
                                                                className="flex items-center justify-between p-3 bg-background border rounded-lg hover:bg-accent/50 transition-colors"
                                                            >
                                                                <div className="flex items-center gap-3">
                                                                    <span className="text-lg">{getFileIcon(file.name)}</span>
                                                                    <div className="space-y-1">
                                                                        <p className="text-sm font-medium truncate max-w-[200px]">
                                                                            {file.name}
                                                                        </p>
                                                                        <div className="flex items-center gap-2">
                                                                            <Badge variant="outline" className="text-xs">
                                                                                {formatFileSize(file.size)}
                                                                            </Badge>
                                                                            <Badge variant="secondary" className="text-xs">
                                                                                {file.name.split('.').pop()?.toUpperCase() || "FILE"}
                                                                            </Badge>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <Button
                                                                    type="button"
                                                                    variant="ghost"
                                                                    size="icon"
                                                                    onClick={() => removeDockerFile(index)}
                                                                    className="h-8 w-8 text-destructive hover:text-destructive/80 hover:bg-destructive/10"
                                                                >
                                                                    <X className="h-4 w-4" />
                                                                </Button>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>

                            {/* Hints Section */}
                            <div className="space-y-4">
                                <div className="flex gap-2">
                                    <Input
                                        type="text"
                                        placeholder="Add a hint..."
                                        value={newHint}
                                        onChange={(e) => setNewHint(e.target.value)}
                                        onKeyPress={(e) => {
                                            if (e.key === 'Enter' && newHint.trim()) {
                                                setHints([...hints, newHint.trim()])
                                                setNewHint("")
                                            }
                                        }}
                                        className="flex-1"
                                    />
                                    <Button
                                        type="button"
                                        onClick={() => {
                                            if (newHint.trim()) {
                                                setHints([...hints, newHint.trim()])
                                                setNewHint("")
                                            }
                                        }}
                                        disabled={!newHint.trim()}
                                    >
                                        Add Hint
                                    </Button>
                                </div>

                                {hints.length > 0 && (
                                    <div className="space-y-2">
                                        <p className="text-sm text-muted-foreground">Hints ({hints.length}):</p>
                                        <div className="space-y-2">
                                            {hints.map((hint, index) => (
                                                <div key={index} className="flex items-center justify-between p-2 bg-muted rounded">
                                                    <span className="text-sm">{hint}</span>
                                                    <Button
                                                        type="button"
                                                        variant="ghost"
                                                        size="icon"
                                                        onClick={() => setHints(hints.filter((_, i) => i !== index))}
                                                        className="h-6 w-6 text-destructive hover:text-destructive/80 hover:bg-destructive/10"
                                                    >
                                                        <X className="h-3 w-3" />
                                                    </Button>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <Button
                                type="button"
                                onClick={form.handleSubmit(onSubmit)}
                                disabled={isLoading}
                                className="w-full"
                                size="lg"
                            >
                                {isLoading ? (
                                    <>
                                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                        Creating Challenge...
                                    </>
                                ) : (
                                    "Create Challenge"
                                )}
                            </Button>
                        </div>
                    </Form>
                </CardContent>
            </Card>
        </div>
    )
}