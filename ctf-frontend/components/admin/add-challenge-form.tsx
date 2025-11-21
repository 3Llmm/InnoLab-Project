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
import { Terminal, Monitor, Code, Server } from "lucide-react"
import { createChallenge } from "@/lib/api/challenges"
import { useToast } from "@/hooks/use-toast"

// Form validation schema
const challengeFormSchema = z.object({
  title: z.string().min(1, "Title is required"),
  description: z.string().min(1, "Description is required"),
  category: z.string().min(1, "Category is required"),
  difficulty: z.enum(["easy", "medium", "hard"]),
  points: z.number().min(1, "Points must be at least 1"),
  flag: z.string().min(1, "Flag is required"),
  file: z.instanceof(File).refine((file) => file.size > 0, "File is required"),
  
  // Instance fields
  requiresInstance: z.boolean().default(false),
  dockerImageName: z.string().optional(),
  defaultSshPort: z.number().min(1024).max(65535).optional(),
  defaultVscodePort: z.number().min(1024).max(65535).optional(),
  defaultDesktopPort: z.number().min(1024).max(65535).optional(),
})

type ChallengeFormValues = z.infer<typeof challengeFormSchema>

export default function AddChallengeForm() {
  const [isLoading, setIsLoading] = useState(false)
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
      defaultSshPort: 30000,
      defaultVscodePort: 31000,
      defaultDesktopPort: 32000,
    },
  })

  const requiresInstance = form.watch("requiresInstance")

  const onSubmit = async (data: ChallengeFormValues) => {
    setIsLoading(true)
    try {
      await createChallenge({
        title: data.title,
        description: data.description,
        category: data.category,
        difficulty: data.difficulty,
        points: data.points,
        flag: data.flag,
        file: data.file,
        requiresInstance: data.requiresInstance,
        dockerImageName: data.requiresInstance ? data.dockerImageName : undefined,
        defaultSshPort: data.requiresInstance ? data.defaultSshPort : undefined,
        defaultVscodePort: data.requiresInstance ? data.defaultVscodePort : undefined,
        defaultDesktopPort: data.requiresInstance ? data.defaultDesktopPort : undefined,
      })

      toast({
        title: "Challenge created!",
        description: "The challenge has been successfully created.",
      })

      form.reset()
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create challenge",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create New Challenge</CardTitle>
        <CardDescription>
          Add a new CTF challenge. Choose between static download or instance-based challenges.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Title</FormLabel>
                    <FormControl>
                      <Input placeholder="Web SQL Injection" {...field} />
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
                        <SelectItem value="web-exploitation">Web Exploitation</SelectItem>
                        <SelectItem value="cryptography">Cryptography</SelectItem>
                        <SelectItem value="reverse-engineering">Reverse Engineering</SelectItem>
                        <SelectItem value="binary-exploitation">Binary Exploitation</SelectItem>
                        <SelectItem value="forensics">Forensics</SelectItem>
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
                    <FormLabel>Flag</FormLabel>
                    <FormControl>
                      <Input placeholder="FLAG{example_flag}" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {/* File Upload */}
            <FormField
              control={form.control}
              name="file"
              render={({ field: { value, onChange, ...field } }) => (
                <FormItem>
                  <FormLabel>Challenge Files (ZIP)</FormLabel>
                  <FormControl>
                    <Input
                      type="file"
                      accept="*"
                      onChange={(e) => onChange(e.target.files?.[0])}
                      {...field}
                    />
                  </FormControl>
                  <FormDescription>
                    Upload a ZIP file containing the challenge files
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
                      <FormLabel className="text-base">Requires Running Instance</FormLabel>
                      <FormDescription>
                        Enable if this challenge needs a Docker container with SSH, VSCode, or Desktop access
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
                <Alert>
                  <Server className="h-4 w-4" />
                  <AlertDescription>
                    Instance-based challenge: Players will get a dedicated Docker container
                  </AlertDescription>
                </Alert>
              )}

              {requiresInstance && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 border rounded-lg p-6 bg-muted/50">
                  <FormField
                    control={form.control}
                    name="dockerImageName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Docker Image Name</FormLabel>
                        <FormControl>
                          <Input placeholder="ctf/web-challenge" {...field} />
                        </FormControl>
                        <FormDescription>
                          Docker image that will be run for this challenge
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <div className="space-y-4">
                    <FormField
                      control={form.control}
                      name="defaultSshPort"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="flex items-center gap-2">
                            <Terminal className="h-4 w-4" />
                            SSH Port
                          </FormLabel>
                          <FormControl>
                            <Input
                              type="number"
                              placeholder="30000"
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
                      name="defaultVscodePort"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="flex items-center gap-2">
                            <Code className="h-4 w-4" />
                            VSCode Port
                          </FormLabel>
                          <FormControl>
                            <Input
                              type="number"
                              placeholder="31000"
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
                      name="defaultDesktopPort"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel className="flex items-center gap-2">
                            <Monitor className="h-4 w-4" />
                            Desktop Port
                          </FormLabel>
                          <FormControl>
                            <Input
                              type="number"
                              placeholder="32000"
                              {...field}
                              onChange={(e) => field.onChange(parseInt(e.target.value) || 0)}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>
                </div>
              )}
            </div>

            <Button type="submit" disabled={isLoading} className="w-full">
              {isLoading ? "Creating Challenge..." : "Create Challenge"}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}